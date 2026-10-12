package GUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;

/**
 * Shared colors, fonts and small custom-painted components for the
 * AI-generated modern UI layer. Pure Swing, no external libraries.
 */
public final class Theme {

	private Theme() {}

	// Palette
	public static final Color BG          = new Color(0xF1F5F4);
	public static final Color SURFACE     = Color.WHITE;
	public static final Color SIDEBAR     = new Color(0x0F2A27);
	public static final Color SIDEBAR_HI  = new Color(0x1B403B);
	public static final Color SIDEBAR_TXT = new Color(0xB9D3CD);
	public static final Color ACCENT      = new Color(0x0E7C6B);
	public static final Color ACCENT_DARK = new Color(0x0A6153);
	public static final Color TEXT        = new Color(0x10201D);
	public static final Color MUTED       = new Color(0x51645F);
	public static final Color BORDER      = new Color(0xD8E2DF);
	public static final Color BUBBLE_THEM = new Color(0xFFFFFF);
	public static final Color AUDIT       = new Color(0xC26A06);

	// Fonts
	public static final Font TITLE = new Font("SansSerif", Font.BOLD, 26);
	public static final Font H2    = new Font("SansSerif", Font.BOLD, 16);
	public static final Font BODY  = new Font("SansSerif", Font.PLAIN, 14);
	public static final Font SMALL = new Font("SansSerif", Font.PLAIN, 11);

	/** Rounded rectangle border, optionally filled by the owning component. */
	public static class RoundBorder extends AbstractBorder {
		private final Color color;
		private final int radius;
		private final int pad;

		public RoundBorder(Color color, int radius, int pad) {
			this.color = color;
			this.radius = radius;
			this.pad = pad;
		}

		@Override
		public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int w, int h) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(color);
			g2.setStroke(new BasicStroke(1.2f));
			g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
			g2.dispose();
		}

		@Override
		public Insets getBorderInsets(java.awt.Component c) {
			return new Insets(pad, pad + 4, pad, pad + 4);
		}
	}

	/** Panel that paints a rounded, filled background. */
	public static class RoundPanel extends JPanel {
		private Color fill;
		private final int radius;

		public RoundPanel(Color fill, int radius) {
			this.fill = fill;
			this.radius = radius;
			setOpaque(false);
		}

		public void setFill(Color fill) {
			this.fill = fill;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(fill);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
			g2.dispose();
			super.paintComponent(g);
		}
	}

	/** Flat pill-shaped button with hover state. */
	public static JButton button(String text, Color bg, Color fg) {
		JButton b = new JButton(text) {
			private boolean hover;
			{
				addMouseListener(new MouseAdapter() {
					public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
					public void mouseExited(MouseEvent e) { hover = false; repaint(); }
				});
			}

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(hover ? bg.darker() : bg);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		b.setFont(H2.deriveFont(14f));
		b.setForeground(fg);
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setFocusPainted(false);
		b.setOpaque(false);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
		return b;
	}

	public static JButton primary(String text) {
		return button(text, ACCENT, Color.WHITE);
	}

	public static JButton secondary(String text) {
		return button(text, new Color(0xD5ECE7), TEXT);
	}

	public static JTextField field(int cols) {
		JTextField f = new JTextField(cols);
		styleField(f);
		return f;
	}

	public static JPasswordField passwordField(int cols) {
		JPasswordField f = new JPasswordField(cols);
		styleField(f);
		return f;
	}

	private static void styleField(JComponent f) {
		f.setFont(BODY);
		f.setBorder(new RoundBorder(BORDER, 14, 8));
		f.setBackground(SURFACE);
		f.setPreferredSize(new Dimension(280, 40));
	}

	/** Circular avatar with the user's initial. */
	public static JComponent avatar(String name, int size, Color bg) {
		String initial = (name == null || name.isEmpty()) ? "?" : name.substring(0, 1).toUpperCase();
		JComponent c = new JComponent() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.setColor(bg);
				g2.fillOval(0, 0, size, size);
				g2.setColor(Color.WHITE);
				g2.setFont(H2.deriveFont(size * 0.42f));
				int tw = g2.getFontMetrics().stringWidth(initial);
				int th = g2.getFontMetrics().getAscent();
				g2.drawString(initial, (size - tw) / 2, (size + th) / 2 - 2);
				g2.dispose();
			}
		};
		Dimension d = new Dimension(size, size);
		c.setPreferredSize(d);
		c.setMinimumSize(d);
		c.setMaximumSize(d);
		return c;
	}

	/** Deterministic avatar color per name. */
	public static Color colorFor(String name) {
		Color[] palette = {
			new Color(0x0E7C6B), new Color(0xB4532A), new Color(0x5B5BD6),
			new Color(0xA3366F), new Color(0x2F6FA3), new Color(0x7A6A12)
		};
		int h = name == null ? 0 : Math.abs(name.hashCode());
		return palette[h % palette.length];
	}
}
