package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import networking.Client;

/**
 * Entry point for the AI-generated modern UI layer.
 *
 * Shows a small start screen with a Classic / Modern slider, then launches
 * either the original {@link GUI} (unchanged) or {@link ModernGUI}.
 * The original {@link Main} still launches the classic UI directly.
 *
 * Accepts the same optional single IP argument as Main.
 */
public class Launcher {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> showChooser(args));
	}

	private static void showChooser(String[] args) {
		JFrame frame = new JFrame("Communication System — Choose UI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(440, 340);
		frame.setLocationRelativeTo(null);

		JPanel bg = new JPanel(new GridBagLayout());
		bg.setBackground(Theme.SIDEBAR);

		Theme.RoundPanel card = new Theme.RoundPanel(Theme.SURFACE, 26);
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(BorderFactory.createEmptyBorder(26, 30, 24, 30));

		JLabel title = new JLabel("Communication System");
		title.setFont(Theme.TITLE.deriveFont(22f));
		title.setForeground(Theme.TEXT);
		JLabel sub = new JLabel("Choose which interface to launch");
		sub.setFont(Theme.BODY);
		sub.setForeground(Theme.MUTED);

		ToggleSwitch toggle = new ToggleSwitch();
		toggle.setSelected(true);
		JLabel classic = new JLabel("Classic");
		JLabel modern = new JLabel("Modern");
		classic.setFont(Theme.H2.deriveFont(14f));
		modern.setFont(Theme.H2.deriveFont(14f));
		Runnable refresh = () -> {
			classic.setForeground(toggle.isSelected() ? Theme.MUTED : Theme.TEXT);
			modern.setForeground(toggle.isSelected() ? Theme.ACCENT : Theme.MUTED);
		};
		toggle.onChange = refresh;
		refresh.run();
		classic.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		modern.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		classic.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) { toggle.setSelected(false); }
		});
		modern.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) { toggle.setSelected(true); }
		});

		JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
		switchRow.setOpaque(false);
		switchRow.add(classic);
		switchRow.add(toggle);
		switchRow.add(modern);

		JLabel note = new JLabel("<html><div style='width:260px;color:#6B7280'>Classic is the original"
				+ " CS 401 interface. Modern is an AI-generated restyle that uses the same client code.</div></html>");
		note.setFont(Theme.SMALL.deriveFont(12f));

		JButton launch = Theme.primary("Launch");
		launch.addActionListener(e -> {
			boolean useModern = toggle.isSelected();
			frame.dispose();
			try {
				Client client = new Client(args);
				if (useModern)
					new ModernGUI(client);
				else
					new GUI(client);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null,
						"Could not connect to the server.\nStart networking.Server first, then try again.\n\n" + ex,
						"Connection failed", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		});

		for (JComponent c : new JComponent[] { title, sub, switchRow, note, launch })
			c.setAlignmentX(Component.CENTER_ALIGNMENT);
		launch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

		card.add(title);
		card.add(Box.createVerticalStrut(4));
		card.add(sub);
		card.add(Box.createVerticalStrut(24));
		card.add(switchRow);
		card.add(Box.createVerticalStrut(18));
		card.add(note);
		card.add(Box.createVerticalStrut(20));
		card.add(launch);
		card.setPreferredSize(new Dimension(360, 270));

		bg.add(card);
		frame.add(bg, BorderLayout.CENTER);
		frame.setVisible(true);
	}

	/** Animated iOS-style slider switch. */
	static class ToggleSwitch extends JComponent {
		private boolean selected;
		private float pos; // 0 = left (classic), 1 = right (modern)
		private Timer anim;
		Runnable onChange;

		ToggleSwitch() {
			setPreferredSize(new Dimension(58, 30));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) { setSelected(!selected); }
			});
		}

		boolean isSelected() {
			return selected;
		}

		void setSelected(boolean value) {
			boolean first = anim == null;
			selected = value;
			if (onChange != null)
				onChange.run();
			if (first) {
				pos = value ? 1 : 0;
				anim = new Timer(12, null);
				anim.addActionListener(e -> {
					float target = selected ? 1 : 0;
					pos += (target - pos) * 0.3f;
					if (Math.abs(target - pos) < 0.01f) {
						pos = target;
						anim.stop();
					}
					repaint();
				});
			} else {
				anim.start();
			}
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth(), h = getHeight();
			Color off = new Color(0xCBD5E1);
			Color on = Theme.ACCENT;
			g2.setColor(new Color(
					(int) (off.getRed() + (on.getRed() - off.getRed()) * pos),
					(int) (off.getGreen() + (on.getGreen() - off.getGreen()) * pos),
					(int) (off.getBlue() + (on.getBlue() - off.getBlue()) * pos)));
			g2.fillRoundRect(0, 0, w, h, h, h);
			int d = h - 6;
			int x = 3 + (int) ((w - d - 6) * pos);
			g2.setColor(Color.WHITE);
			g2.fillOval(x, 3, d, d);
			g2.dispose();
		}
	}
}
