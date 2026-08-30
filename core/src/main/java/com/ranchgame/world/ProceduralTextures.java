package com.ranchgame.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Disposable;

/**
 * Soft painterly surface textures generated at startup (no asset files).
 * Everything is value noise layered over simple patterns, sized 128x128
 * (power-of-two, so WebGL1 can tile and mipmap them).
 */
public class ProceduralTextures implements Disposable {

    public final Texture grass;
    public final Texture dirt;
    /** Painterly cobblestones with grassy grout, Star Stable style. */
    public final Texture cobble;
    public final Texture wood;
    public final Texture planks;
    public final Texture shingles;
    public final Texture leaves;
    /** Near-white dappled noise, meant to be tinted by each horse's coat color. */
    public final Texture coat;
    /** Vertical sky gradient for the sky dome (v=0 bottom pole, v=1 top pole). */
    public final Texture sky;

    private static final int S = 128;

    public ProceduralTextures() {
        grass = make(new Painter() {
            public Color pixel(int x, int y, float n1, float n2) {
                // natural meadow green: deeper, less neon, softer blotches
                float l = 0.88f + n1 * 0.24f + n2 * 0.08f;
                return new Color(0.30f * l, 0.52f * l, 0.22f * l, 1f);
            }
        }, 11, 5, 20260722L);
        cobble = makeCobble();
        dirt = make(new Painter() {
            public Color pixel(int x, int y, float n1, float n2) {
                float l = 0.85f + n1 * 0.22f + n2 * 0.18f;
                return new Color(0.64f * l, 0.52f * l, 0.36f * l, 1f);
            }
        }, 9, 4, 77L);
        wood = make(new Painter() {
            public Color pixel(int x, int y, float n1, float n2) {
                // vertical grain: luminance varies by column with wobble
                float grain = MathUtils.sin((x + n1 * 14f) * 0.55f) * 0.08f;
                float l = 0.86f + grain + n2 * 0.14f;
                return new Color(0.58f * l, 0.42f * l, 0.26f * l, 1f);
            }
        }, 6, 3, 402L);
        planks = make(new Painter() {
            public Color pixel(int x, int y, float n1, float n2) {
                float l = 0.9f + n1 * 0.14f;
                if (x % 32 < 2) l *= 0.72f;               // plank seams
                if ((y + (x / 32) * 7) % 64 < 2) l *= 0.85f; // staggered board ends
                return new Color(0.74f * l, 0.24f * l, 0.2f * l, 1f);
            }
        }, 8, 4, 8181L);
        shingles = make(new Painter() {
            public Color pixel(int x, int y, float n1, float n2) {
                float l = 0.88f + n1 * 0.16f;
                if (y % 16 < 2) l *= 0.68f;               // shingle rows
                if ((x + (y / 16) * 8) % 24 < 2) l *= 0.82f; // offset gaps
                return new Color(0.45f * l, 0.33f * l, 0.27f * l, 1f);
            }
        }, 7, 3, 909L);
        leaves = make(new Painter() {
            public Color pixel(int x, int y, float n1, float n2) {
                float l = 0.72f + n1 * 0.45f + n2 * 0.2f;
                return new Color(0.2f * l, 0.5f * l, 0.22f * l, 1f);
            }
        }, 13, 6, 3434L);
        coat = make(new Painter() {
            public Color pixel(int x, int y, float n1, float n2) {
                // soft base shading plus clustered lighter dapple spots
                float spot = MathUtils.clamp((n2 - 0.1f) / 0.09f, 0f, 1f);
                spot = spot * spot * (3f - 2f * spot);
                float l = 0.93f + n1 * 0.05f + spot * 0.075f;
                return new Color(l, l * 0.995f, l * 0.985f, 1f);
            }
        }, 9, 14, 606L);
        sky = makeSky();
    }

    /**
     * Worley-noise cobblestones: warm grey stones of varied tone with dark
     * grassy grout between them, wrapping seamlessly.
     */
    private static Texture makeCobble() {
        int size = 256;
        int cells = 6;
        RandomXS128 rng = new RandomXS128(4242L);
        float[][] ptx = new float[cells][cells];
        float[][] pty = new float[cells][cells];
        float[][] tone = new float[cells][cells];
        for (int cy = 0; cy < cells; cy++) {
            for (int cx = 0; cx < cells; cx++) {
                ptx[cy][cx] = (cx + 0.2f + rng.nextFloat() * 0.6f) / cells;
                pty[cy][cx] = (cy + 0.2f + rng.nextFloat() * 0.6f) / cells;
                tone[cy][cx] = rng.nextFloat();
            }
        }
        float[][] n = noiseGrid(24, 999L);
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        Color grout = new Color(0.33f, 0.36f, 0.26f, 1f);
        Color c = new Color();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float fx = x / (float) size;
                float fy = y / (float) size;
                int cellX = (int) (fx * cells);
                int cellY = (int) (fy * cells);
                float f1 = Float.MAX_VALUE, f2 = Float.MAX_VALUE;
                float nearTone = 0f;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = ((cellX + dx) % cells + cells) % cells;
                        int ny = ((cellY + dy) % cells + cells) % cells;
                        // shift the wrapped point into this neighborhood
                        float px = ptx[ny][nx] + (cellX + dx < 0 ? -1f : cellX + dx >= cells ? 1f : 0f);
                        float py = pty[ny][nx] + (cellY + dy < 0 ? -1f : cellY + dy >= cells ? 1f : 0f);
                        float d = (fx - px) * (fx - px) + (fy - py) * (fy - py);
                        if (d < f1) {
                            f2 = f1;
                            f1 = d;
                            nearTone = tone[ny][nx];
                        } else if (d < f2) {
                            f2 = d;
                        }
                    }
                }
                float edge = (float) (Math.sqrt(f2) - Math.sqrt(f1));
                float t = MathUtils.clamp((edge - 0.008f) / 0.022f, 0f, 1f);
                t = t * t * (3f - 2f * t);
                float l = 0.6f + nearTone * 0.38f + sampleAt(n, x, y, size) * 0.15f;
                c.set(0.74f * l, 0.71f * l, 0.66f * l, 1f).lerp(grout, 1f - t);
                pm.setColor(c);
                pm.drawPixel(x, y);
            }
        }
        Texture t = new Texture(pm, true);
        pm.dispose();
        t.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        t.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        return t;
    }

    private static final Color ZENITH = new Color(0.3f, 0.55f, 0.86f, 1f);
    private static final Color HORIZON = new Color(0.8f, 0.89f, 0.97f, 1f);

    private static Texture makeSky() {
        int h = 256;
        Pixmap pm = new Pixmap(4, h, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        Color c = new Color();
        for (int y = 0; y < h; y++) {
            // v: 0 = bottom pole, 0.5 = horizon, 1 = zenith
            float v = 1f - y / (float) (h - 1);
            float t = MathUtils.clamp((v - 0.5f) * 2.2f, 0f, 1f);
            t = t * t * (3f - 2f * t);
            c.set(HORIZON).lerp(ZENITH, t);
            pm.setColor(c);
            pm.drawLine(0, y, 3, y);
        }
        Texture t = new Texture(pm, false);
        pm.dispose();
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        return t;
    }

    private interface Painter {
        Color pixel(int x, int y, float lowFreqNoise, float highFreqNoise);
    }

    private static Texture make(Painter painter, int cells1, int cells2, long seed) {
        float[][] n1 = noiseGrid(cells1, seed);
        float[][] n2 = noiseGrid(cells2 * 4, seed * 31 + 7);
        Pixmap pm = new Pixmap(S, S, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < S; y++) {
            for (int x = 0; x < S; x++) {
                Color c = painter.pixel(x, y, sample(n1, x, y), sample(n2, x, y));
                pm.setColor(MathUtils.clamp(c.r, 0f, 1f), MathUtils.clamp(c.g, 0f, 1f),
                        MathUtils.clamp(c.b, 0f, 1f), 1f);
                pm.drawPixel(x, y);
            }
        }
        Texture t = new Texture(pm, true);
        pm.dispose();
        t.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        t.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        return t;
    }

    /** Tileable value-noise: random lattice sampled with bilinear + smoothstep. */
    private static float[][] noiseGrid(int cells, long seed) {
        RandomXS128 rng = new RandomXS128(seed);
        float[][] g = new float[cells][cells];
        for (int y = 0; y < cells; y++) {
            for (int x = 0; x < cells; x++) {
                g[y][x] = rng.nextFloat() - 0.5f;
            }
        }
        return g;
    }

    private static float sample(float[][] g, int px, int py) {
        return sampleAt(g, px, py, S);
    }

    private static float sampleAt(float[][] g, int px, int py, int size) {
        int cells = g.length;
        float fx = px * (float) cells / size;
        float fy = py * (float) cells / size;
        int x0 = (int) fx, y0 = (int) fy;
        float tx = smooth(fx - x0), ty = smooth(fy - y0);
        int x1 = (x0 + 1) % cells, y1 = (y0 + 1) % cells;
        x0 %= cells;
        y0 %= cells;
        float a = g[y0][x0] * (1 - tx) + g[y0][x1] * tx;
        float b = g[y1][x0] * (1 - tx) + g[y1][x1] * tx;
        return a * (1 - ty) + b * ty;
    }

    private static float smooth(float t) {
        return t * t * (3f - 2f * t);
    }

    @Override
    public void dispose() {
        grass.dispose();
        dirt.dispose();
        cobble.dispose();
        wood.dispose();
        planks.dispose();
        shingles.dispose();
        leaves.dispose();
        coat.dispose();
        sky.dispose();
    }
}
