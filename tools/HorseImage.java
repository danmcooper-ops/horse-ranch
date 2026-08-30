import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Draws a black mare — black coat, long black mane and tail, white eyes with
 * black irises — as a 1200x900 PNG using nothing but Java2D.
 *
 * Run it with a plain JDK (11+), no build tool needed:
 *
 *     java tools/HorseImage.java [output.png]
 *
 * The image is written to horse.png (or the path given as first argument).
 */
public final class HorseImage {

    static final int W = 1200, H = 900;
    static final int GROUND = 770;

    // Coat and mane are both black; slightly different shades keep them readable.
    static final Color COAT       = new Color(0x26262b);
    static final Color COAT_DARK  = new Color(0x131316);
    static final Color COAT_SHEEN = new Color(0x4a4a54);
    static final Color MANE       = new Color(0x08080a);
    static final Color MANE_HILITE= new Color(0x35353d);
    static final Color HOOF       = new Color(0x2e2e33);

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        drawBackground(g);
        drawHorse(g);

        g.dispose();
        File out = new File(args.length > 0 ? args[0] : "horse.png");
        ImageIO.write(img, "png", out);
        System.out.println("Wrote " + out.getAbsolutePath());
    }

    static void drawBackground(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, new Color(0xb8d4ee), 0, GROUND, new Color(0xeaf3fb)));
        g.fillRect(0, 0, W, GROUND);
        g.setPaint(new GradientPaint(0, GROUND, new Color(0x7fa75f), 0, H, new Color(0x567f43)));
        g.fillRect(0, GROUND, W, H - GROUND);
        // soft shadow under the horse
        g.setColor(new Color(0, 0, 0, 45));
        g.fill(new Ellipse2D.Double(310, GROUND - 18, 600, 60));
    }

    static void drawHorse(Graphics2D g) {
        drawFarLegs(g);
        drawTail(g);
        drawBody(g);
        drawNearLegs(g);
        drawHeadAndNeck(g);
        drawMane(g);
        drawEye(g);
    }

    // ---- body -------------------------------------------------------------

    static void drawBody(Graphics2D g) {
        Area body = new Area(new Ellipse2D.Double(390, 330, 440, 250)); // barrel
        body.add(new Area(new Ellipse2D.Double(360, 340, 210, 220)));   // chest/shoulder
        body.add(new Area(new Ellipse2D.Double(660, 320, 230, 245)));   // hindquarters

        g.setPaint(new GradientPaint(600, 320, COAT_SHEEN, 600, 580, COAT_DARK));
        g.fill(body);

        // shading stays inside the silhouette
        java.awt.Shape oldClip = g.getClip();
        g.setClip(body);
        // belly shading, fading upward
        g.setPaint(new GradientPaint(600, 580, new Color(0, 0, 0, 90), 600, 460, new Color(0, 0, 0, 0)));
        g.fillRect(360, 440, 540, 150);
        // soft sheen along the back
        g.setPaint(new GradientPaint(600, 325, new Color(255, 255, 255, 34), 600, 430, new Color(255, 255, 255, 0)));
        g.fillRect(360, 320, 540, 120);
        g.setClip(oldClip);
    }

    // ---- head & neck ------------------------------------------------------

    static void drawHeadAndNeck(Graphics2D g) {
        // neck: from poll down to the shoulder
        Path2D neck = new Path2D.Double();
        neck.moveTo(330, 155);                       // poll (top of head)
        neck.curveTo(400, 200, 440, 280, 470, 370);  // crest -> withers
        neck.lineTo(560, 500);
        neck.curveTo(480, 520, 400, 500, 380, 470);  // into the chest
        neck.curveTo(330, 380, 300, 290, 292, 235);  // throat line back up
        neck.closePath();
        g.setPaint(new GradientPaint(300, 150, COAT_SHEEN, 440, 430, new Color(0x1b1b1f)));
        g.fill(neck);

        // head: forehead -> muzzle -> jaw -> cheek
        Path2D head = new Path2D.Double();
        head.moveTo(332, 148);                        // poll
        head.curveTo(300, 150, 270, 165, 250, 195);   // forehead
        head.curveTo(230, 225, 212, 250, 198, 268);   // nose bridge
        head.curveTo(188, 280, 186, 298, 200, 305);   // muzzle tip
        head.curveTo(214, 312, 232, 308, 244, 298);   // lips / chin
        head.curveTo(262, 284, 286, 268, 306, 252);   // jaw line
        head.curveTo(330, 232, 348, 205, 350, 180);   // round cheek
        head.curveTo(350, 162, 344, 150, 332, 148);
        head.closePath();
        g.setPaint(new GradientPaint(220, 160, COAT_SHEEN, 320, 300, COAT));
        g.fill(head);

        // nostril
        g.setColor(new Color(0, 0, 0, 160));
        g.fill(new Ellipse2D.Double(206, 280, 12, 9));
        // mouth line
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.QuadCurve2D.Double(200, 300, 216, 306, 234, 302));

        // ears
        Path2D ear = new Path2D.Double();
        ear.moveTo(318, 152); ear.quadTo(310, 110, 322, 92);
        ear.quadTo(336, 112, 336, 150); ear.closePath();
        g.setColor(COAT); g.fill(ear);
        Path2D ear2 = new Path2D.Double();
        ear2.moveTo(342, 152); ear2.quadTo(344, 112, 356, 96);
        ear2.quadTo(366, 118, 360, 152); ear2.closePath();
        g.setColor(COAT_DARK); g.fill(ear2);
    }

    // ---- eye: white with a black iris ------------------------------------

    static void drawEye(Graphics2D g) {
        double cx = 281, cy = 199;
        // white of the eye
        g.setColor(Color.WHITE);
        Ellipse2D sclera = new Ellipse2D.Double(cx - 14, cy - 9, 28, 18);
        g.fill(sclera);
        // black iris with pupil-sized depth
        g.setColor(Color.BLACK);
        g.fill(new Ellipse2D.Double(cx - 6.5, cy - 6.5, 13, 13));
        // tiny catchlight so the eye looks alive
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(cx - 4, cy - 4.5, 4, 4));
        // lid outline
        g.setColor(new Color(0x0a0a0c));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(sclera);
    }

    // ---- long black mane --------------------------------------------------

    static void drawMane(Graphics2D g) {
        // forelock between the ears, falling over the forehead
        Path2D forelock = new Path2D.Double();
        forelock.moveTo(318, 140);
        forelock.curveTo(290, 150, 272, 168, 266, 190);
        forelock.quadTo(276, 182, 282, 186);
        forelock.quadTo(288, 168, 304, 158);
        forelock.quadTo(316, 152, 330, 150);
        forelock.closePath();
        g.setColor(MANE);
        g.fill(forelock);

        // the mane proper: rooted on the crest, cascading down the near side
        Path2D mane = new Path2D.Double();
        mane.moveTo(336, 142);                        // behind the ears
        mane.curveTo(410, 185, 452, 260, 480, 350);   // along the crest
        mane.curveTo(492, 388, 500, 420, 505, 440);   // over the withers
        // flowing bottom edge back toward the head, in long waves
        mane.curveTo(482, 470, 462, 490, 458, 520);   // longest strand tip
        mane.quadTo(452, 490, 446, 468);
        mane.quadTo(436, 492, 428, 512);
        mane.quadTo(424, 478, 416, 452);
        mane.quadTo(404, 474, 396, 492);
        mane.quadTo(392, 452, 384, 424);
        mane.quadTo(372, 442, 364, 458);
        mane.quadTo(360, 418, 350, 386);
        mane.quadTo(338, 402, 332, 416);
        mane.quadTo(328, 372, 318, 336);
        mane.quadTo(308, 348, 302, 360);
        mane.curveTo(298, 300, 312, 200, 330, 148);
        mane.closePath();
        g.setColor(MANE);
        g.fill(mane);

        // a few lighter strands so the black mane reads against the black coat
        g.setColor(MANE_HILITE);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.QuadCurve2D.Double(346, 170, 400, 260, 440, 430));
        g.draw(new java.awt.geom.QuadCurve2D.Double(336, 190, 380, 280, 400, 450));
        g.draw(new java.awt.geom.QuadCurve2D.Double(326, 210, 352, 300, 350, 420));
        g.draw(new java.awt.geom.QuadCurve2D.Double(320, 230, 330, 300, 322, 390));
    }

    // ---- long black tail --------------------------------------------------

    static void drawTail(Graphics2D g) {
        Path2D tail = new Path2D.Double();
        tail.moveTo(860, 350);                        // dock, high on the rump
        tail.curveTo(920, 380, 950, 470, 946, 560);
        tail.curveTo(944, 640, 928, 710, 900, 755);   // sweeps almost to the ground
        tail.quadTo(912, 700, 908, 640);
        tail.quadTo(896, 690, 884, 730);
        tail.quadTo(892, 660, 886, 600);
        tail.quadTo(872, 650, 862, 690);
        tail.quadTo(866, 600, 856, 540);
        tail.quadTo(844, 580, 838, 620);
        tail.curveTo(830, 540, 828, 440, 842, 360);
        tail.closePath();
        g.setColor(MANE);
        g.fill(tail);

        g.setColor(MANE_HILITE);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.QuadCurve2D.Double(870, 380, 920, 500, 900, 720));
        g.draw(new java.awt.geom.QuadCurve2D.Double(856, 390, 890, 520, 866, 660));
    }

    // ---- legs -------------------------------------------------------------

    /** One leg as a tapering path; hind legs get an angled hock. */
    static void leg(Graphics2D g, Color c, double x, boolean hind) {
        Path2D p = new Path2D.Double();
        if (!hind) {
            p.moveTo(x - 26, 470);
            p.curveTo(x - 24, 560, x - 16, 620, x - 13, 660);  // forearm -> knee
            p.lineTo(x - 12, 740);                             // cannon
            p.lineTo(x + 12, 740);
            p.lineTo(x + 11, 660);
            p.curveTo(x + 16, 620, x + 24, 560, x + 30, 470);
        } else {
            p.moveTo(x - 34, 460);
            p.curveTo(x - 26, 550, x - 4, 600, x + 8, 645);    // gaskin -> hock
            p.lineTo(x + 2, 740);                              // cannon drops forward
            p.lineTo(x + 26, 740);
            p.lineTo(x + 30, 648);
            p.curveTo(x + 34, 590, x + 36, 520, x + 34, 460);
        }
        p.closePath();
        g.setColor(c);
        g.fill(p);
        // hoof
        double hx = hind ? x + 2 : x - 14;
        g.setColor(hind ? HOOF : (c == COAT_DARK ? new Color(0x1d1d21) : HOOF));
        Path2D hoof = new Path2D.Double();
        hoof.moveTo(hx, 740);
        hoof.lineTo(hx - 4, GROUND);
        hoof.lineTo(hx + 32, GROUND);
        hoof.lineTo(hx + 26, 740);
        hoof.closePath();
        g.fill(hoof);
    }

    static void drawFarLegs(Graphics2D g) {
        leg(g, COAT_DARK, 475, false);
        leg(g, COAT_DARK, 795, true);
    }

    static void drawNearLegs(Graphics2D g) {
        leg(g, COAT, 420, false);
        leg(g, COAT, 745, true);
    }

    private HorseImage() {}
}
