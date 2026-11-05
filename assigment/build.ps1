param([string]$Task = "run")
$root = $PSScriptRoot
$out = Join-Path $root "out"
if (Test-Path $out) { Remove-Item $out -Recurse -Force }
New-Item -ItemType Directory $out | Out-Null

$main = Get-ChildItem "$root\src\main\java" -Recurse -Filter *.java | ForEach-Object FullName
javac -d $out $main
if ($LASTEXITCODE -ne 0) { exit 1 }

switch ($Task) {
    "test" {
        $test = Get-ChildItem "$root\src\test\java" -Recurse -Filter *.java | ForEach-Object FullName
        javac -cp $out -d $out $test
        if ($LASTEXITCODE -ne 0) { exit 1 }
        java -cp $out banking.BankTest
        exit $LASTEXITCODE
    }
    "testds" {
        $test = Get-ChildItem "$root\src\test\java\bankds" -Recurse -Filter *.java | ForEach-Object FullName
        javac -cp $out -d $out $test
        if ($LASTEXITCODE -ne 0) { exit 1 }
        java -cp $out bankds.BankingDsTest
        exit $LASTEXITCODE
    }
    "runds" { java -cp $out bankds.Main }
    "run" { java -cp $out banking.Main }
    default { Write-Host "Usage: .\build.ps1 [run|test]" }
}
