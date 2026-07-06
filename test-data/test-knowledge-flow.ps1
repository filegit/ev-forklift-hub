# Knowledge base E2E test
$ErrorActionPreference = "Stop"
$base = "http://127.0.0.1:8080"
$testDir = "d:\LH\ev-forklift-hub\test-data"

function Get-Token($user, $pass) {
    $loginFile = "$testDir\login-$user.json"
    if (-not (Test-Path $loginFile)) {
        @{username=$user;password=$pass} | ConvertTo-Json | Set-Content $loginFile -Encoding UTF8
    }
    $r = curl.exe -s -m 15 -X POST "$base/user/api/login" -H "Content-Type: application/json" -d "@$loginFile" | ConvertFrom-Json
    if ($r.code -ne 200) { throw "Login failed for $user : $($r.message)" }
    return $r.data.token
}

Write-Host "=== 1. Admin login ===" -ForegroundColor Cyan
$adminToken = Get-Token "kbadmin" "123456"
Write-Host "OK token length $($adminToken.Length)"

Write-Host "=== 2. Upload test document ===" -ForegroundColor Cyan
$uploadRaw = curl.exe -s -m 30 -X POST "$base/knowledge/api/knowledge/admin/doc" `
    -H "Authorization: Bearer $adminToken" `
    -F "file=@$testDir/knowledge-test-doc.txt" `
    -F "meta=@$testDir/knowledge-meta.json;type=application/json"
$upload = $uploadRaw | ConvertFrom-Json
if ($upload.code -ne 200) { throw "Upload failed: $uploadRaw" }
$docId = $upload.data.id
Write-Host "OK docId=$docId title=$($upload.data.title)"

Write-Host "=== 3. Publish document ===" -ForegroundColor Cyan
$pubRaw = curl.exe -s -m 15 -X POST "$base/knowledge/api/knowledge/admin/doc/$docId/publish" -H "Authorization: Bearer $adminToken"
$pub = $pubRaw | ConvertFrom-Json
if ($pub.code -ne 200) { throw "Publish failed: $pubRaw" }
Write-Host "OK published"

Write-Host "=== 4. User login ===" -ForegroundColor Cyan
$userToken = Get-Token "shopuser4" "123456"
Write-Host "OK"

Write-Host "=== 5. List published docs ===" -ForegroundColor Cyan
$listRaw = curl.exe -s -m 15 "$base/knowledge/api/knowledge/doc/list?page=1&size=10" -H "Authorization: Bearer $userToken"
$list = $listRaw | ConvertFrom-Json
if ($list.code -ne 200) { throw "List failed: $listRaw" }
$found = $list.data.records | Where-Object { $_.id -eq $docId }
if (-not $found) { throw "Doc not in list" }
Write-Host "OK found doc unlocked=$($found.unlocked) label=$($found.accessLabel)"

Write-Host "=== 6. Doc detail before unlock ===" -ForegroundColor Cyan
$detRaw = curl.exe -s -m 15 "$base/knowledge/api/knowledge/doc/$docId" -H "Authorization: Bearer $userToken"
$det = $detRaw | ConvertFrom-Json
if ($det.data.unlocked) { throw "Should not be unlocked yet" }
Write-Host "OK unlocked=false"

Write-Host "=== 7. Points before unlock ===" -ForegroundColor Cyan
$ptsRaw = curl.exe -s -m 15 "$base/user/api/points" -H "Authorization: Bearer $userToken"
$pts = $ptsRaw | ConvertFrom-Json
$beforePts = $pts.data.availablePoints
Write-Host "OK availablePoints=$beforePts"

Write-Host "=== 8. Unlock with points ===" -ForegroundColor Cyan
$unlockRaw = curl.exe -s -m 20 -X POST "$base/knowledge/api/knowledge/doc/$docId/unlock/points" -H "Authorization: Bearer $userToken"
$unlock = $unlockRaw | ConvertFrom-Json
if ($unlock.code -ne 200) { throw "Unlock failed: $unlockRaw" }
Write-Host "OK unlocked"

Write-Host "=== 9. Verify points deducted ===" -ForegroundColor Cyan
$pts2Raw = curl.exe -s -m 15 "$base/user/api/points" -H "Authorization: Bearer $userToken"
$pts2 = $pts2Raw | ConvertFrom-Json
$afterPts = $pts2.data.availablePoints
Write-Host "OK before=$beforePts after=$afterPts diff=$($beforePts - $afterPts)"

Write-Host "=== 10. Doc detail after unlock ===" -ForegroundColor Cyan
$det2Raw = curl.exe -s -m 15 "$base/knowledge/api/knowledge/doc/$docId" -H "Authorization: Bearer $userToken"
$det2 = $det2Raw | ConvertFrom-Json
if (-not $det2.data.unlocked) { throw "Should be unlocked" }
Write-Host "OK unlocked=true"

Write-Host "=== 11. Download document ===" -ForegroundColor Cyan
$outFile = "$testDir\downloaded-$docId.txt"
curl.exe -s -m 20 "$base/knowledge/api/knowledge/doc/$docId/download" -H "Authorization: Bearer $userToken" -o $outFile
if (-not (Test-Path $outFile)) { throw "Download file missing" }
$size = (Get-Item $outFile).Length
if ($size -lt 200) { throw "Download file too small: $size bytes" }
Write-Host "OK downloaded $size bytes"

Write-Host ""
Write-Host "ALL TESTS PASSED" -ForegroundColor Green
