# 构建阶段
FROM node:18 as build-stage

WORKDIR /app

# 使用国内 npm 镜像
RUN npm config set registry https://registry.npmmirror.com

# 复制 package.json 和 package-lock.json
COPY package*.json ./

# 安装依赖（Alpine 下需完整安装 devDependencies 才能 vite build）
RUN npm install --registry=https://registry.npmmirror.com --legacy-peer-deps \
    && test -f node_modules/.bin/vite

# 复制项目文件
COPY . .

# 构建项目
RUN npx vite build

# 生产阶段
FROM nginx:alpine as production-stage

# 复制构建产物到 nginx
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
