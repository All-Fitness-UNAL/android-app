"""python -I tree.py canvas.json [frame-name-substring] — prints the node tree compactly."""
import json
import sys

data = json.load(open(sys.argv[1], encoding='utf-8'))
want = sys.argv[2] if len(sys.argv) > 2 else None
nodes = {}
children = {}


def key(g):
    return f"{g['sessionID']}:{g['localID']}"


for n in data['nodeChanges']:
    nodes[key(n['guid'])] = n
for n in data['nodeChanges']:
    p = n.get('parentIndex')
    if p:
        children.setdefault(key(p['guid']), []).append((p.get('position', ''), key(n['guid'])))
for k in children:
    children[k].sort()


def hexc(c, op=1):
    a = c.get('a', 1) * op
    s = '#%02X%02X%02X' % (round(c['r'] * 255), round(c['g'] * 255), round(c['b'] * 255))
    return s if a >= 0.999 else f"{s}@{a:.2f}"


def paints(ps):
    out = []
    for p in ps or []:
        if p.get('visible') is False:
            continue
        t = p.get('type')
        if t == 'SOLID':
            out.append(hexc(p['color'], p.get('opacity', 1)))
        elif t == 'IMAGE':
            h = p.get('image', {}).get('hash')
            out.append('IMG:' + (h[:8] if isinstance(h, str) else '?'))
        elif t and t.startswith('GRADIENT'):
            stops = ','.join(hexc(s['color']) for s in p.get('stops', []))
            out.append(f"{t[9:].lower()}({stops})")
    return out


def line(n, depth):
    t = n.get('type')
    name = n.get('name', '')
    sz = n.get('size', {})
    tr = n.get('transform', {})
    bits = [f"{'  ' * depth}{t} '{name}' {sz.get('x', 0):.0f}x{sz.get('y', 0):.0f} @{tr.get('m02', 0):.0f},{tr.get('m12', 0):.0f}"]
    f = paints(n.get('fillPaints'))
    if f:
        bits.append('fill=' + '|'.join(f))
    s = paints(n.get('strokePaints'))
    if s and n.get('strokeWeight'):
        bits.append(f"stroke={'|'.join(s)}/{n.get('strokeWeight')}")
    if n.get('cornerRadius'):
        bits.append(f"r={n['cornerRadius']:.0f}")
    if n.get('opacity', 1) < 1:
        bits.append(f"op={n['opacity']:.2f}")
    if t == 'TEXT':
        fn = n.get('fontName', {})
        bits.append(f"[{fn.get('family')} {fn.get('style')} {n.get('fontSize', 0):.0f}] \"{n.get('textData', {}).get('characters', '')[:120]}\"")
    for e in n.get('effects') or []:
        if e.get('visible') is not False:
            bits.append(f"fx={e.get('type')}")
    return ' '.join(bits)


def walk(k, depth):
    n = nodes[k]
    print(line(n, depth))
    if n.get('type') in ('VECTOR', 'BOOLEAN_OPERATION', 'INSTANCE') and depth > 1:
        return
    for _, c in children.get(k, []):
        walk(c, depth + 1)


canvases = [k for k, n in nodes.items() if n.get('type') == 'CANVAS']
for c in canvases:
    print('CANVAS', nodes[c].get('name'))
    for _, f in children.get(c, []):
        if want is None:
            print(line(nodes[f], 1))
        elif want.lower() in nodes[f].get('name', '').lower():
            walk(f, 1)
