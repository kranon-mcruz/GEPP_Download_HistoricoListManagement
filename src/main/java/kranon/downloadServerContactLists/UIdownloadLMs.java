package kranon.downloadServerContactLists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.border.BevelBorder;

public class UIdownloadLMs {

	private static final String PASSWORD = "Kr4n0n01#";
	private boolean autenticado = false;
	private JFrame frmDownloadContactlist;
	private JTextArea textAreaConsole;
	private JDateChooser dateChooser;
	private JButton btnExecute;
	private JButton btnCancel;
	private JProgressBar progressBar;
	private Thread procesoThread;
	private JButton btnClearConsole;
	private JPasswordField passwordField;
	private JLabel lblPassword;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIdownloadLMs window = new UIdownloadLMs();
					window.frmDownloadContactlist.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public UIdownloadLMs() {
		initialize();
	}

	private void initialize() {
		frmDownloadContactlist = new JFrame();
		frmDownloadContactlist.setTitle("Download ContactLists From GEPP Server");
		frmDownloadContactlist.setIconImage(Toolkit.getDefaultToolkit()
				.getImage(UIdownloadLMs.class.getResource("/kranon/image/kranon-lcono.png")));
		frmDownloadContactlist.setBounds(100, 100, 501, 513);
		frmDownloadContactlist.setLocationRelativeTo(null);
		frmDownloadContactlist.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmDownloadContactlist.getContentPane().setLayout(null);
		frmDownloadContactlist.setResizable(false);

		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBounds(0, 0, 492, 476);
		frmDownloadContactlist.getContentPane().add(panel);
		panel.setLayout(null);

		JLabel lblTittle = new JLabel("DESCARGA DE LISTAS DE MARCACIÓN");
		lblTittle.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
		lblTittle.setBounds(87, 100, 342, 25);
		panel.add(lblTittle);

		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(0, 0, 128));
		panel_1.setBounds(-5, 0, 540, 85);
		panel.add(panel_1);
		panel_1.setLayout(null);

		lblPassword = new JLabel("Ingrese Contraseña:");
		lblPassword.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
		lblPassword.setBounds(100, 130, 130, 25);
		panel.add(lblPassword);

		passwordField = new JPasswordField();
		passwordField.setBounds(233, 134, 150, 16);
		panel.add(passwordField);

		passwordField.addActionListener(e -> {
			String input = new String(passwordField.getPassword());

			if (input.equals(PASSWORD)) {
				autenticado = true;
				JOptionPane.showMessageDialog(frmDownloadContactlist, "Autenticación correcta");
				passwordField.setEnabled(false);
			} else {
				autenticado = false;
				JOptionPane.showMessageDialog(frmDownloadContactlist, "Contraseña incorrecta");
				passwordField.setText("");
			}
		});

		JLabel lblFecha = new JLabel("Seleccione fecha:");
		lblFecha.setFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
		lblFecha.setBounds(100, 155, 120, 25);
		panel.add(lblFecha);

		// GUARDAR REFERENCIA
		dateChooser = new JDateChooser();
		dateChooser.getCalendarButton().setFont(new Font("Roboto", Font.PLAIN, 11));
		dateChooser.setBounds(233, 158, 150, 18);
		panel.add(dateChooser);
		// Bloquear escritura manual
		((JTextField) dateChooser.getDateEditor().getUiComponent()).setEditable(false);

		JLabel llbKranito = new JLabel("");
		llbKranito.setIcon(
				new ImageIcon(UIdownloadLMs.class.getResource("/kranon/image/Kranon-Robot-servicio-al-cliente.png")));
		llbKranito.setBounds(20, -33, 100, 148);
		panel_1.add(llbKranito);

		JLabel lblLogo = new JLabel("");
		lblLogo.setIcon(new ImageIcon(UIdownloadLMs.class.getResource("/kranon/image/kranon-logo.png")));
		lblLogo.setBounds(92, -24, 140, 135);
		panel_1.add(lblLogo);

		JLabel lblLogoGEPP = new JLabel("");
		lblLogoGEPP.setBounds(346, -8, 140, 104);
		panel_1.add(lblLogoGEPP);
		lblLogoGEPP.setIcon(new ImageIcon(UIdownloadLMs.class.getResource("/kranon/image/LogoGEPP.png")));

		// GUARDAR BOTON
		btnExecute = new JButton("EJECUTAR");
		btnExecute.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
		btnExecute.setBounds(186, 190, 107, 23);
		// Cambiar colores
		btnExecute.setBackground(new Color(0x005AA8)); // fondo azul
		btnExecute.setForeground(Color.WHITE); // letras blancas
		btnExecute.setOpaque(true); // necesario para que se vea el fondo
		btnExecute.setBorderPainted(false);
		panel.add(btnExecute);

		// GUARDAR CONSOLA
		textAreaConsole = new JTextArea();
		textAreaConsole.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
		textAreaConsole.setEditable(false);
		textAreaConsole.setBackground(SystemColor.menu);
		textAreaConsole.setBounds(18, 230, 450, 126);
		panel.add(textAreaConsole);

//		 Scroll pane para la consola
		JScrollPane scrollPane = new JScrollPane(textAreaConsole);
		scrollPane.setBounds(30, 230, 428, 126);
		panel.add(scrollPane);
		// REDIRIGIR CONSOLA
		redirectSystemStreams(textAreaConsole);

		progressBar = new JProgressBar();
		progressBar.setFont(new Font("Microsoft JhengHei", Font.BOLD, 8));
		progressBar.setBounds(70, 375, 230, 23);
		progressBar.setStringPainted(true); // muestra porcentaje
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setForeground(new Color(0, 128, 0));
		panel.add(progressBar);

		btnCancel = new JButton("CANCELAR");
		btnCancel.setOpaque(true);
		btnCancel.setForeground(Color.WHITE);
		btnCancel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
		btnCancel.setBorderPainted(false);
		btnCancel.setBackground(Color.RED);
		btnCancel.setBounds(186, 418, 107, 23);
		btnCancel.setEnabled(false);
		panel.add(btnCancel);

		btnClearConsole = new JButton("LIMPIAR");
		btnClearConsole.setBounds(310, 375, 102, 23);
		btnClearConsole.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
		btnClearConsole.setBackground(Color.LIGHT_GRAY);
		btnClearConsole.setForeground(Color.WHITE);
		btnClearConsole.setOpaque(true);
		btnClearConsole.setBorderPainted(false);
		panel.add(btnClearConsole);

		// EVENTO BOTON
		btnClearConsole.addActionListener(e -> {
			textAreaConsole.setText("");
			progressBar.setValue(0);
			progressBar.setString("");
			dateChooser.setDate(null);
		});

		btnExecute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = new String(passwordField.getPassword());

				if (input.isEmpty()) {
					JOptionPane.showMessageDialog(frmDownloadContactlist, "Ingrese contraseña");
					return;
				}

				if (!input.equals(PASSWORD)) {
					JOptionPane.showMessageDialog(frmDownloadContactlist, "Contraseña incorrecta");
					passwordField.setText("");
					return;
				}

				ejecutarProceso();
			}
		});

		btnCancel.addActionListener(e -> {
			if (procesoThread != null && procesoThread.isAlive()) {
				procesoThread.interrupt();
				btnCancel.setEnabled(false);
				progressBar.setValue(0);
				progressBar.setString("");
//				resetUI();
			}
		});
	}

	// LOGICA PRINCIPAL
	private void ejecutarProceso() {

//---		new Thread(() -> {
		procesoThread = new Thread(() -> {
			try {

				btnExecute.setEnabled(false);
				btnCancel.setEnabled(true);

				// Fecha
				Date date = dateChooser.getDate();
				if (date == null) {
					JOptionPane.showMessageDialog(frmDownloadContactlist, "Selecciona una fecha");
					return;
				}

				LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				String fecha = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

				System.out.println("[INFO] Iniciando proceso...");

				// Descargar
//				Map<String, String> zips = DownloadService.descargarYZip(fecha);
				Map<String, String> zips = DownloadService.descargarYZip(fecha, (archivo, leidos, total) -> {

					int porcentaje = (int) ((leidos * 100) / total);

					SwingUtilities.invokeLater(() -> {
						progressBar.setValue(porcentaje);
						progressBar.setString("Descargando: " + archivo + " (" + porcentaje + "%)");
					});
				});

				String zipUSA = zips.get("USA");
				String zipCA = zips.get("CANADA");

			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				btnExecute.setEnabled(true);
				btnCancel.setEnabled(false);
			}
//---		}).start();
		});

		procesoThread.start();
	}

	private List<String> parseCorreos(String input) {
		return Arrays.stream(input.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
	}

	// REDIRIGIR CONSOLA A TEXTAREA
	private void redirectSystemStreams(JTextArea textArea) {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) {
				SwingUtilities.invokeLater(() -> { // boton cancelar
					textArea.append(String.valueOf((char) b));
					textArea.setCaretPosition(textArea.getDocument().getLength());
				});
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}

	private void resetUI() {
		SwingUtilities.invokeLater(() -> {
			progressBar.setValue(0);
			progressBar.setString("");
			textAreaConsole.setText("");

			btnExecute.setEnabled(true);
			btnCancel.setEnabled(false);
		});
	}
}