param(
    [string]$BaseUrl = "http://127.0.0.1:18080",
    [string]$CasesPath = "docs/agent-eval-cases.json",
    [string]$UserId = "1",
    [switch]$FormatOnly
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$caseFile = Join-Path $root $CasesPath

if (!(Test-Path $caseFile)) {
    throw "Eval cases not found: $caseFile"
}

$cases = Get-Content -Path $caseFile -Raw -Encoding UTF8 | ConvertFrom-Json
if (!$cases -or $cases.Count -lt 1) {
    throw "Eval cases are empty"
}

$requiredFields = @("id", "category", "question")
foreach ($case in $cases) {
    foreach ($field in $requiredFields) {
        if (-not $case.$field) {
            throw "Case missing '$field': $($case | ConvertTo-Json -Compress)"
        }
    }
}

if ($FormatOnly) {
    Write-Host "Agent eval case format OK. Case count: $($cases.Count)"
    exit 0
}

$passed = 0
$failed = 0
$results = @()

foreach ($case in $cases) {
    $body = @{
        question = $case.question
        scope = "all"
        imageUrls = @()
        allowCreateTicket = $false
    }
    if ($case.imageUrls) {
        $body.imageUrls = @($case.imageUrls)
    }

    try {
        $resp = Invoke-RestMethod `
            -Method Post `
            -Uri "$BaseUrl/api/agent/api/agent/chat" `
            -Headers @{ "X-User-Id" = $UserId } `
            -ContentType "application/json; charset=utf-8" `
            -Body ($body | ConvertTo-Json -Depth 8)

        $data = $resp.data
        $answer = [string]$data.answer
        $casePassed = $true
        $reason = "OK"

        if ($case.expectedIntent -and $data.intent -ne $case.expectedIntent) {
            $casePassed = $false
            $reason = "Intent expected=$($case.expectedIntent), actual=$($data.intent)"
        }

        if ($case.mustContainAny) {
            $hit = $false
            foreach ($word in $case.mustContainAny) {
                if ($answer.Contains([string]$word)) {
                    $hit = $true
                    break
                }
            }
            if (-not $hit) {
                $casePassed = $false
                $reason = "Answer did not contain any expected keyword"
            }
        }

        if ($casePassed) { $passed++ } else { $failed++ }
        $results += [pscustomobject]@{
            id = $case.id
            category = $case.category
            passed = $casePassed
            reason = $reason
            intent = $data.intent
            traceId = $data.traceId
        }
    } catch {
        if ($case.expectedBlocked) {
            $passed++
            $results += [pscustomobject]@{
                id = $case.id
                category = $case.category
                passed = $true
                reason = "Blocked as expected"
                intent = ""
                traceId = ""
            }
        } else {
            $failed++
            $results += [pscustomobject]@{
                id = $case.id
                category = $case.category
                passed = $false
                reason = $_.Exception.Message
                intent = ""
                traceId = ""
            }
        }
    }
}

$results | Format-Table -AutoSize
Write-Host "Agent eval finished. Passed=$passed Failed=$failed Total=$($cases.Count)"
if ($failed -gt 0) {
    exit 1
}
