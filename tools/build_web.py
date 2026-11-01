"""Turn game/wallshove.html into a standalone site in site/ and the APK's offline copy.

Usage: python3 tools/build_web.py [build-number]
"""
import pathlib, shutil, sys

root = pathlib.Path(__file__).resolve().parent.parent
build = sys.argv[1] if len(sys.argv) > 1 else "dev"
game = (root / "game" / "wallshove.html").read_text(encoding="utf-8").replace("__BUILD__", build)

head = """<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover, user-scalable=no">
<meta name="theme-color" content="#14121c">
<meta name="mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-capable" content="yes">
<link rel="manifest" href="manifest.webmanifest">
<link rel="icon" href="icon.svg" type="image/svg+xml">
<link rel="apple-touch-icon" href="icon-192.png">
<style>
:root{color-scheme:light;padding-top:env(safe-area-inset-top,0px);padding-bottom:env(safe-area-inset-bottom,0px);box-sizing:border-box;height:100%}
body{margin:0;font:14px system-ui,sans-serif}
img{max-width:100%}
[hidden]{display:none!important}
</style>
</head>
<body>
"""
tail = """
<script>
if ('serviceWorker' in navigator && location.protocol === 'https:') navigator.serviceWorker.register('sw.js').catch(() => {});
</script>
</body>
</html>
"""
page = head + game + tail

site = root / "site"
shutil.rmtree(site, ignore_errors=True)
site.mkdir()
(site / "index.html").write_text(page, encoding="utf-8")
for f in (root / "web").iterdir():
    data = f.read_bytes()
    if f.suffix in (".js", ".webmanifest"):
        data = data.replace(b"__BUILD__", build.encode())
    (site / f.name).write_bytes(data)
(site / ".nojekyll").write_text("")

assets = root / "android" / "app" / "src" / "main" / "assets"
assets.mkdir(parents=True, exist_ok=True)
(assets / "index.html").write_text(page.replace("<link rel=\"manifest\" href=\"manifest.webmanifest\">\n", ""), encoding="utf-8")
print(f"Built site/ and the APK's offline copy (build {build})")
