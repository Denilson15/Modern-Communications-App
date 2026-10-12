package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.*;

import chat.Chat;
import chat.TextMessage;
import networking.Client;
import user.User;

/**
 * AI-generated modern skin for the original client.
 *
 * Extends the original {@link GUI} so the networking.Client callbacks
 * (reloadChatList, setNewAuditUser, removeCurrentChat, forceExit) keep working
 * without any change to the original source. Only the visual layer is replaced;
 * every action is forwarded to the same Client / User methods the original uses.
 *
 * Note: fields here intentionally have no initializers, because the GUI
 * super-constructor calls buildGUI() before subclass initializers would run.
 */
public class ModernGUI extends GUI {

	private int selectedChatId;
	private boolean hasSelection;
	private JPanel chatList;
	private JPanel messages;
	private JScrollPane messageScroll;
	private JLabel headerTitle;
	private JLabel headerSub;
	private JButton headerMenuButton;

	public ModernGUI(Client client) throws UnknownHostException, IOException {
		super(client);
	}

	// ---------------------------------------------------------------- login

	@Override
	public void buildGUI() {
		loginFrame = new JFrame("Communication System");
		loginFrame.setSize(460, 640);
		loginFrame.setLocationRelativeTo(null);
		loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel bg = new JPanel(new GridBagLayout());
		bg.setBackground(Theme.SIDEBAR);

		Theme.RoundPanel card = new Theme.RoundPanel(Theme.SURFACE, 28);
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(BorderFactory.createEmptyBorder(32, 32, 28, 32));

		JComponent logo = Theme.avatar("C", 56, Theme.ACCENT);
		JLabel title = new JLabel("Welcome back");
		title.setFont(Theme.TITLE);
		title.setForeground(Theme.TEXT);
		JLabel sub = new JLabel("Sign in to continue chatting");
		sub.setFont(Theme.BODY);
		sub.setForeground(Theme.MUTED);

		JTextField usernameField = Theme.field(16);
		JPasswordField passwordField = Theme.passwordField(16);
		JCheckBox itBox = new JCheckBox("IT account");
		itBox.setOpaque(false);
		itBox.setFont(Theme.BODY);
		itBox.setForeground(Theme.MUTED);

		JButton loginB = Theme.primary("Log in");
		JButton createB = Theme.secondary("Create account");

		loginB.addActionListener(e -> {
			user = new User(usernameField.getText(), new String(passwordField.getPassword()), itBox.isSelected());
			try {
				login(); // original GUI.login(): authenticates, then calls createMainFrame()
			} catch (ClassNotFoundException | IOException ex) {
				ex.printStackTrace();
			}
		});
		passwordField.addActionListener(e -> loginB.doClick());

		createB.addActionListener(e -> {
			user = new User(usernameField.getText(), new String(passwordField.getPassword()), itBox.isSelected());
			try {
				if (createNewAccount()) {
					JOptionPane.showMessageDialog(loginFrame,
							"Your account was successfully created. Please log in!",
							"Account Created", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(loginFrame,
							"Please provide a unique username. Username and password must be a minimum of 6 characters in length.",
							"Account Creation Failed", JOptionPane.ERROR_MESSAGE);
				}
			} catch (IOException | ClassNotFoundException ex) {
				ex.printStackTrace();
			}
		});

		for (JComponent c : new JComponent[] { logo, title, sub, usernameField, passwordField, itBox, loginB, createB }) {
			c.setAlignmentX(Component.LEFT_ALIGNMENT);
		}
		usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
		passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
		loginB.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		createB.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

		card.add(logo);
		card.add(Box.createVerticalStrut(18));
		card.add(title);
		card.add(Box.createVerticalStrut(4));
		card.add(sub);
		card.add(Box.createVerticalStrut(24));
		card.add(label("Username"));
		card.add(Box.createVerticalStrut(6));
		card.add(usernameField);
		card.add(Box.createVerticalStrut(14));
		card.add(label("Password"));
		card.add(Box.createVerticalStrut(6));
		card.add(passwordField);
		card.add(Box.createVerticalStrut(10));
		card.add(itBox);
		card.add(Box.createVerticalStrut(18));
		card.add(loginB);
		card.add(Box.createVerticalStrut(10));
		card.add(createB);
		card.setPreferredSize(new Dimension(360, 520));

		bg.add(card);
		loginFrame.setContentPane(bg);
		loginFrame.setVisible(true);
	}

	private static JLabel label(String text) {
		JLabel l = new JLabel(text);
		l.setFont(Theme.SMALL.deriveFont(java.awt.Font.BOLD, 12f));
		l.setForeground(Theme.MUTED);
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		return l;
	}

	// ----------------------------------------------------------- main frame

	@Override
	public void createMainFrame() {
		mainFrame = new JFrame("Communication System" + (auditMode ? " — Audit Mode" : ""));
		mainFrame.setSize(1000, 680);
		mainFrame.setMinimumSize(new Dimension(820, 520));
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				try {
					client.logout();
					Thread.sleep(300);
				} catch (Exception e) {
					e.printStackTrace();
				}
				mainFrame.dispose();
				System.exit(0);
			}
		});

		JPanel root = new JPanel(new BorderLayout());
		root.add(buildSidebar(), BorderLayout.WEST);
		root.add(buildConversation(), BorderLayout.CENTER);
		mainFrame.setContentPane(root);

		hasSelection = false;
		reloadChatList(user);
		mainFrame.setVisible(true);
	}

	private JPanel buildSidebar() {
		JPanel side = new JPanel(new BorderLayout());
		side.setBackground(Theme.SIDEBAR);
		side.setPreferredSize(new Dimension(280, 0));

		// top: title + action
		JPanel top = new JPanel(new BorderLayout(0, 12));
		top.setOpaque(false);
		top.setBorder(BorderFactory.createEmptyBorder(20, 18, 12, 18));
		JLabel appName = new JLabel(auditMode ? "Audit Mode" : "Chats");
		appName.setFont(Theme.TITLE.deriveFont(22f));
		appName.setForeground(auditMode ? Theme.AUDIT : Color.WHITE);
		top.add(appName, BorderLayout.NORTH);

		JButton action;
		if (auditMode) {
			action = Theme.button("Select user to audit", Theme.AUDIT, Color.WHITE);
			action.addActionListener(e -> {
				String username = JOptionPane.showInputDialog(mainFrame, "Enter username:", "Select User",
						JOptionPane.PLAIN_MESSAGE);
				if (username != null && !username.trim().isEmpty()) {
					try {
						client.audit_SelectUser(username.trim());
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			});
		} else {
			action = Theme.primary("+  New chat");
			action.addActionListener(e -> {
				String usernames = JOptionPane.showInputDialog(mainFrame, "Enter usernames separated by commas:",
						"New Chat", JOptionPane.PLAIN_MESSAGE);
				if (usernames != null) {
					try {
						client.createChat(usernames);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			});
		}
		top.add(action, BorderLayout.CENTER);
		side.add(top, BorderLayout.NORTH);

		// middle: chat list
		chatList = new JPanel();
		chatList.setLayout(new BoxLayout(chatList, BoxLayout.Y_AXIS));
		chatList.setBackground(Theme.SIDEBAR);
		chatList.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		JScrollPane scroll = new JScrollPane(chatList);
		scroll.setBorder(null);
		scroll.getViewport().setBackground(Theme.SIDEBAR);
		scroll.getVerticalScrollBar().setUnitIncrement(14);
		side.add(scroll, BorderLayout.CENTER);

		// bottom: profile
		JPanel profile = new JPanel(new BorderLayout(10, 0));
		profile.setBackground(Theme.SIDEBAR_HI);
		profile.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
		profile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		profile.add(Theme.avatar(user.getUsername(), 36, Theme.colorFor(user.getUsername())), BorderLayout.WEST);
		JPanel names = new JPanel();
		names.setOpaque(false);
		names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
		JLabel n = new JLabel(user.getUsername());
		n.setFont(Theme.H2.deriveFont(14f));
		n.setForeground(Color.WHITE);
		JLabel r = new JLabel(auditMode ? "Auditing · click for options"
				: (user.isInformationTechnologyUser() ? "IT staff · click for options" : "Click for options"));
		r.setFont(Theme.SMALL);
		r.setForeground(Theme.SIDEBAR_TXT);
		names.add(n);
		names.add(r);
		profile.add(names, BorderLayout.CENTER);
		profile.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				profileMenu().show(profile, e.getX(), e.getY() - 60);
			}
		});
		side.add(profile, BorderLayout.SOUTH);
		return side;
	}

	private JPanel buildConversation() {
		JPanel conv = new JPanel(new BorderLayout());
		conv.setBackground(Theme.BG);

		// header
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(Theme.SURFACE);
		header.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
				BorderFactory.createEmptyBorder(14, 20, 14, 16)));
		JPanel titles = new JPanel();
		titles.setOpaque(false);
		titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
		headerTitle = new JLabel("Select a chat");
		headerTitle.setFont(Theme.H2);
		headerTitle.setForeground(Theme.TEXT);
		headerSub = new JLabel("Pick a conversation from the left");
		headerSub.setFont(Theme.SMALL);
		headerSub.setForeground(Theme.MUTED);
		titles.add(headerTitle);
		titles.add(headerSub);
		header.add(titles, BorderLayout.CENTER);

		headerMenuButton = Theme.secondary("Manage");
		headerMenuButton.setVisible(false);
		headerMenuButton.addActionListener(e -> chatMenu().show(headerMenuButton, 0, headerMenuButton.getHeight()));
		header.add(headerMenuButton, BorderLayout.EAST);
		conv.add(header, BorderLayout.NORTH);

		// messages
		messages = new JPanel();
		messages.setLayout(new BoxLayout(messages, BoxLayout.Y_AXIS));
		messages.setBackground(Theme.BG);
		messages.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
		messageScroll = new JScrollPane(messages);
		messageScroll.setBorder(null);
		messageScroll.getVerticalScrollBar().setUnitIncrement(16);
		messageScroll.getViewport().setBackground(Theme.BG);
		conv.add(messageScroll, BorderLayout.CENTER);

		// composer
		JPanel composer = new JPanel(new BorderLayout(10, 0));
		composer.setBackground(Theme.SURFACE);
		composer.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
				BorderFactory.createEmptyBorder(12, 16, 12, 16)));
		if (auditMode) {
			JLabel note = new JLabel("Read-only audit view");
			note.setFont(Theme.BODY);
			note.setForeground(Theme.MUTED);
			JButton export = Theme.button("Export chat log", Theme.AUDIT, Color.WHITE);
			export.addActionListener(e -> {
				if (!hasSelection)
					return;
				user.exportChat(selectedChatId, false);
				JOptionPane.showMessageDialog(mainFrame, "Exported to LocalFiles/IT_Export/Chats");
			});
			composer.add(note, BorderLayout.CENTER);
			composer.add(export, BorderLayout.EAST);
		} else {
			JTextField input = Theme.field(20);
			input.setPreferredSize(new Dimension(0, 42));
			JButton send = Theme.primary("Send");
			Runnable doSend = () -> {
				String text = input.getText();
				if (!hasSelection || text.trim().isEmpty())
					return;
				try {
					client.sendMessage(text, selectedChatId);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				input.setText("");
			};
			send.addActionListener(e -> doSend.run());
			input.addActionListener(e -> doSend.run());
			composer.add(input, BorderLayout.CENTER);
			composer.add(send, BorderLayout.EAST);
		}
		conv.add(composer, BorderLayout.SOUTH);
		return conv;
	}

	// ------------------------------------------------------------ chat list

	@Override
	public void reloadChatList(User u) {
		if (this.user != u || chatList == null)
			return;
		chatList.removeAll();

		int[] chatIds = u.getChatIds();
		if (chatIds.length == 0) {
			JLabel empty = new JLabel(auditMode ? "Select a user to audit" : "No chats yet");
			empty.setFont(Theme.BODY);
			empty.setForeground(Theme.SIDEBAR_TXT);
			empty.setBorder(BorderFactory.createEmptyBorder(16, 10, 0, 0));
			chatList.add(empty);
		}
		for (int chatId : chatIds) {
			Chat chat;
			try {
				chat = u.getCopyOfChat(chatId);
			} catch (IndexOutOfBoundsException e) {
				continue;
			}
			chatList.add(chatRow(chat, chatId));
			chatList.add(Box.createVerticalStrut(4));
		}
		if (hasSelection)
			displayChat(selectedChatId);
		chatList.revalidate();
		chatList.repaint();
	}

	private String chatName(Chat chat, int chatId) {
		ArrayList<String> others = otherMembers(chat);
		if (others.size() == 1)
			return others.get(0);
		if (others.size() > 1)
			return "Group " + chatId;
		return "Chat " + chatId;
	}

	private ArrayList<String> otherMembers(Chat chat) {
		ArrayList<String> others = new ArrayList<>();
		String[] members = chat.getMembersInChat();
		for (int j = 0; j < chat.getNumMembers(); j++) {
			String name = members[j];
			if (name != null && !name.equals(user.getUsername()) && !others.contains(name))
				others.add(name);
		}
		return others;
	}

	private JComponent chatRow(Chat chat, int chatId) {
		boolean selected = hasSelection && chatId == selectedChatId;
		Theme.RoundPanel row = new Theme.RoundPanel(selected ? Theme.ACCENT : Theme.SIDEBAR, 14);
		row.setLayout(new BorderLayout(10, 0));
		row.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		String name = chatName(chat, chatId);
		row.add(Theme.avatar(name, 38, Theme.colorFor(name)), BorderLayout.WEST);

		JPanel text = new JPanel();
		text.setOpaque(false);
		text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
		JLabel title = new JLabel(name);
		title.setFont(Theme.H2.deriveFont(14f));
		title.setForeground(Color.WHITE);
		String preview = "No messages yet";
		if (chat.getNumMessages() > 0) {
			TextMessage last = chat.getMessage(chat.getNumMessages() - 1);
			preview = last.getUsername() + ": " + last.getText();
			if (preview.length() > 32)
				preview = preview.substring(0, 31) + "…";
		}
		JLabel sub = new JLabel(preview);
		sub.setFont(Theme.SMALL);
		sub.setForeground(selected ? new Color(0xD5ECE7) : Theme.SIDEBAR_TXT);
		text.add(title);
		text.add(Box.createVerticalStrut(2));
		text.add(sub);
		row.add(text, BorderLayout.CENTER);

		row.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				if (!(hasSelection && chatId == selectedChatId))
					row.setFill(Theme.SIDEBAR_HI);
			}

			public void mouseExited(MouseEvent e) {
				if (!(hasSelection && chatId == selectedChatId))
					row.setFill(Theme.SIDEBAR);
			}

			public void mouseClicked(MouseEvent e) {
				selectedChatId = chatId;
				hasSelection = true;
				reloadChatList(user); // repaints selection and calls displayChat
			}
		});
		return row;
	}

	// ------------------------------------------------------------- messages

	@Override
	public void displayChat(int chatId) {
		if (messages == null)
			return;
		Chat chat;
		try {
			chat = user.getCopyOfChat(chatId);
		} catch (Exception e) {
			return;
		}
		selectedChatId = chatId;
		hasSelection = true;

		String name = chatName(chat, chatId);
		headerTitle.setText(name);
		headerSub.setText(chat.getNumMembers() + " members · Chat #" + chatId);
		boolean isCreator = user.getUsername().equals(chat.getCreatorUsername());
		headerMenuButton.setVisible(!auditMode && isCreator && chat.getNumMembers() > 2);

		messages.removeAll();
		String prevSender = null;
		for (int i = 0; i < chat.getNumMessages(); i++) {
			TextMessage msg = chat.getMessage(i);
			boolean mine = msg.getUsername().equals(user.getUsername());
			boolean showName = !msg.getUsername().equals(prevSender);
			messages.add(Box.createVerticalStrut(showName ? 10 : 3));
			messages.add(bubbleRow(msg, mine, showName));
			prevSender = msg.getUsername();
		}
		messages.add(Box.createVerticalGlue());
		messages.revalidate();
		messages.repaint();
		SwingUtilities.invokeLater(() -> {
			JScrollBar bar = messageScroll.getVerticalScrollBar();
			bar.setValue(bar.getMaximum());
		});
	}

	private JComponent bubbleRow(TextMessage msg, boolean mine, boolean showName) {
		JPanel row = new JPanel();
		row.setOpaque(false);
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel stack = new JPanel();
		stack.setOpaque(false);
		stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

		if (showName) {
			JLabel meta = new JLabel((mine ? "You" : msg.getUsername()) + "  ·  " + msg.getTimeSent());
			meta.setFont(Theme.SMALL);
			meta.setForeground(Theme.MUTED);
			meta.setAlignmentX(mine ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
			meta.setBorder(BorderFactory.createEmptyBorder(0, 4, 3, 4));
			stack.add(meta);
		}

		JTextArea ta = new JTextArea(msg.getText());
		ta.setFont(Theme.BODY);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setEditable(false);
		ta.setOpaque(false);
		ta.setForeground(mine ? Color.WHITE : Theme.TEXT);
		FontMetrics fm = ta.getFontMetrics(Theme.BODY);
		int longest = 0;
		for (String line : msg.getText().split("\n"))
			longest = Math.max(longest, fm.stringWidth(line));
		int width = Math.min(380, longest + 6);
		ta.setSize(width, Short.MAX_VALUE);
		Dimension pref = new Dimension(width, ta.getPreferredSize().height);
		ta.setPreferredSize(pref);
		ta.setMaximumSize(pref);

		Theme.RoundPanel bubble = new Theme.RoundPanel(mine ? Theme.ACCENT : Theme.SURFACE, 18);
		bubble.setLayout(new BorderLayout());
		bubble.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
		bubble.add(ta);
		Dimension bd = new Dimension(pref.width + 28, pref.height + 18);
		bubble.setPreferredSize(bd);
		bubble.setMaximumSize(bd);
		bubble.setAlignmentX(mine ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
		stack.add(bubble);
		stack.setMaximumSize(stack.getPreferredSize());

		if (mine) {
			row.add(Box.createHorizontalGlue());
			row.add(stack);
		} else {
			JComponent av = Theme.avatar(msg.getUsername(), 30, Theme.colorFor(msg.getUsername()));
			JPanel avWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			avWrap.setOpaque(false);
			avWrap.add(showName ? av : Box.createRigidArea(new Dimension(30, 30)));
			avWrap.setMaximumSize(new Dimension(30, Integer.MAX_VALUE));
			avWrap.setAlignmentY(Component.BOTTOM_ALIGNMENT);
			stack.setAlignmentY(Component.BOTTOM_ALIGNMENT);
			row.add(avWrap);
			row.add(Box.createHorizontalStrut(8));
			row.add(stack);
			row.add(Box.createHorizontalGlue());
		}
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
		return row;
	}

	// ---------------------------------------------------------------- menus

	private JPopupMenu chatMenu() {
		JPopupMenu menu = new JPopupMenu();
		JMenuItem add = new JMenuItem("Add people to chat");
		JMenuItem remove = new JMenuItem("Remove user from chat");
		add.addActionListener(e -> {
			String username = JOptionPane.showInputDialog(mainFrame, "Enter username to add:");
			if (username != null && !username.trim().isEmpty()) {
				try {
					client.addUserToChat(username.trim(), selectedChatId);
					reloadChatList(user);
					JOptionPane.showMessageDialog(mainFrame, "User added to chat.");
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(mainFrame, "Failed to add user.");
				}
			}
		});
		remove.addActionListener(e -> {
			String username = JOptionPane.showInputDialog(mainFrame, "Enter username to remove:");
			if (username != null && !username.trim().isEmpty()) {
				try {
					client.removeUserFromChat(username.trim(), selectedChatId);
					reloadChatList(user);
					JOptionPane.showMessageDialog(mainFrame, "User removed from chat.");
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(mainFrame,
							"Failed to remove user. Only the chat creator can remove people.");
				}
			}
		});
		menu.add(add);
		menu.add(remove);
		return menu;
	}

	private JPopupMenu profileMenu() {
		JPopupMenu menu = new JPopupMenu();
		if (auditMode) {
			JMenuItem exit = new JMenuItem("Exit Audit Mode");
			exit.addActionListener(e -> {
				auditMode = false;
				user = client.getUser();
				mainFrame.dispose();
				createMainFrame();
			});
			menu.add(exit);
			menu.addSeparator();
		} else if (user.isInformationTechnologyUser()) {
			JMenuItem audit = new JMenuItem("Audit Mode");
			audit.addActionListener(e -> {
				auditMode = true;
				mainFrame.dispose();
				createMainFrame();
			});
			menu.add(audit);
			menu.addSeparator();
		}
		JMenuItem logout = new JMenuItem("Logout");
		logout.addActionListener(e -> {
			auditMode = false;
			try {
				client.logout();
				mainFrame.dispose();
			} catch (ClassNotFoundException | IOException ex) {
				ex.printStackTrace();
			}
		});
		menu.add(logout);
		return menu;
	}

	@Override
	public void removeCurrentChat() {
		hasSelection = false;
		if (headerTitle != null) {
			headerTitle.setText("Select a chat");
			headerSub.setText("Pick a conversation from the left");
			headerMenuButton.setVisible(false);
			messages.removeAll();
			messages.repaint();
		}
	}
}
