// node decode.mjs <kiwi-module-dir> <canvas.fig> <out.json>
import { createRequire } from 'node:module';
import fs from 'node:fs';
import zlib from 'node:zlib';

const [kiwiDir, input, output] = process.argv.slice(2);
const kiwi = createRequire(kiwiDir + '/package.json')('kiwi-schema');
const buf = fs.readFileSync(input);
let offset = 12;
const chunks = [];
while (offset < buf.length) {
  const len = buf.readUInt32LE(offset);
  chunks.push(buf.subarray(offset + 4, offset + 4 + len));
  offset += 4 + len;
}
const inflate = (c) =>
  c[0] === 0x28 && c[1] === 0xb5 && c[2] === 0x2f && c[3] === 0xfd ? zlib.zstdDecompressSync(c) : zlib.inflateRawSync(c);
const schema = kiwi.compileSchema(kiwi.decodeBinarySchema(inflate(chunks[0])));
const message = schema.decodeMessage(inflate(chunks[1]));
fs.writeFileSync(output, JSON.stringify(message, (k, v) => (v instanceof Uint8Array ? Buffer.from(v).toString('hex') : v)));
console.log('nodes', message.nodeChanges.length, 'blobs', (message.blobs || []).length);
