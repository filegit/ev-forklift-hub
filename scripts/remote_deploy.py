#!/usr/bin/env python3
"""Deploy ev-forklift-hub to remote server via SSH/SFTP (password auth)."""
import os
import sys
import tarfile
import tempfile
import subprocess

try:
    import paramiko
except ImportError:
    subprocess.check_call([sys.executable, "-m", "pip", "install", "paramiko", "-q"])
    import paramiko

HOST = "111.170.36.78"
USER = "root"
PASSWORD = os.environ.get("DEPLOY_PASSWORD")
REMOTE_DIR = "/opt/ev-forklift-hub"
LOCAL_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EXCLUDES = {
    ".git", ".idea", "node_modules", "dist", "target", "logs", "uploads",
    "__pycache__", ".DS_Store"
}

INCLUDE = [
    "docker-compose.yml", "docker", "efh-web", "server-deploy.sh", "fix-host-db.sql",
    "deploy.env.example", ".env.example", "docker/mysql/init",
]
JAR_MODULES = ["efh-gateway", "efh-user", "efh-community", "efh-parts", "efh-service", "efh-knowledge", "efh-agent"]


def make_tarball():
    tmp = tempfile.NamedTemporaryFile(suffix=".tar.gz", delete=False)
    tmp.close()
    with tarfile.open(tmp.name, "w:gz") as tar:
        for item in INCLUDE:
            path = os.path.join(LOCAL_ROOT, item)
            if os.path.exists(path):
                tar.add(path, arcname=item, filter=tar_filter)
        for mod in JAR_MODULES:
            tdir = os.path.join(LOCAL_ROOT, mod, "target")
            if not os.path.isdir(tdir):
                continue
            for f in os.listdir(tdir):
                if f.endswith(".jar") and not f.endswith(".original"):
                    full = os.path.join(tdir, f)
                    tar.add(full, arcname=f"{mod}/target/{f}")
    return tmp.name


def tar_filter(info):
    parts = info.name.replace("\\", "/").split("/")
    if any(part in EXCLUDES for part in parts):
        return None
    if info.name.endswith(".tar.gz") or info.name.endswith(".zip"):
        return None
    return info


def run_ssh(client, cmd, timeout=600):
    print(f">>> {cmd[:120]}...")
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode(errors="replace")
    err = stderr.read().decode(errors="replace")
    code = stdout.channel.recv_exit_status()
    if out.strip():
        print(out[-3000:] if len(out) > 3000 else out)
    if err.strip() and code != 0:
        print("STDERR:", err[-2000:])
    return code, out


def main():
    if not PASSWORD:
        raise SystemExit("DEPLOY_PASSWORD is required")

    tarball = make_tarball()
    print(f"Created {tarball} ({os.path.getsize(tarball)//1024//1024} MB)")

    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    print(f"Connecting to {HOST}...")
    client.connect(HOST, username=USER, password=PASSWORD, timeout=30)

    sftp = client.open_sftp()
    run_ssh(client, f"mkdir -p {REMOTE_DIR}")
    remote_tar = f"{REMOTE_DIR}/deploy.tar.gz"
    print(f"Uploading to {remote_tar}...")
    sftp.put(tarball, remote_tar)
    sftp.close()
    os.unlink(tarball)

    cmds = f"""
set -e
cd {REMOTE_DIR}
tar -xzf deploy.tar.gz
rm -f deploy.tar.gz
command -v docker >/dev/null || (curl -fsSL https://get.docker.com | sh && systemctl enable docker && systemctl start docker)
command -v docker-compose >/dev/null || docker compose version >/dev/null 2>&1 || (curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose && chmod +x /usr/local/bin/docker-compose)
docker compose up -d mysql redis nacos 2>/dev/null || docker-compose up -d mysql redis nacos
sleep 25
docker exec -i efh-mysql mysql -uroot -p123456 < fix-host-db.sql 2>/dev/null || true
docker exec -i efh-mysql mysql -uroot -p123456 < docker/mysql/init/06-agent.sql 2>/dev/null || true
# 启动 Java 微服务
mkdir -p logs
for env_file in .env.local alipay.env sms.env; do
  if [ -f "$env_file" ]; then
    set -a
    . "./$env_file"
    set +a
    echo "Loaded $env_file"
  fi
done
for j in efh-gateway/target/*.jar efh-user/target/*.jar efh-community/target/*.jar efh-parts/target/*.jar efh-service/target/*.jar efh-knowledge/target/*.jar efh-agent/target/*.jar; do
  [ -f "$j" ] || continue
  name=$(basename "$j" .jar)
  pids=$(pgrep -f "[/]$name.jar" 2>/dev/null || true)
  [ -n "$pids" ] && kill $pids 2>/dev/null || true
  nohup java -Xms256m -Xmx512m -jar "$j" \
    --spring.cloud.nacos.discovery.enabled=false \
    --spring.cloud.service-registry.auto-registration.enabled=false \
    --spring.cloud.discovery.enabled=false \
    > logs/$name.log 2>&1 &
  echo "Started $j"
done
sleep 15
# 前端
cd efh-web
if command -v npm >/dev/null; then
  npm install --registry=https://registry.npmmirror.com 2>/dev/null || true
  npm run build 2>/dev/null || true
fi
cd {REMOTE_DIR}
docker compose up -d --build web 2>/dev/null || docker-compose up -d --build web 2>/dev/null || true
echo "DEPLOY_DONE"
curl -s -o /dev/null -w "GATEWAY:%{{http_code}}" http://127.0.0.1:8080/user/api/test || echo GATEWAY:fail
"""
    code, out = run_ssh(client, cmds, timeout=900)
    client.close()
    print("\n=== Access URLs ===")
    print(f"  Frontend: http://{HOST}/")
    print(f"  Gateway:  http://{HOST}:8080/")
    print(f"  API test: http://{HOST}:8080/user/api/test")
    return 0 if "DEPLOY_DONE" in out else 1


if __name__ == "__main__":
    sys.exit(main())
