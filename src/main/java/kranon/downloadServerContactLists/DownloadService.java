package kranon.downloadServerContactLists;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DownloadService {

	public interface ProgressListener {
		void onProgress(String archivo, long bytesLeidos, long totalBytes);
	}

	public static Map<String, String> descargarYZip(String fecha, ProgressListener listener) throws Exception {

		Map<String, String> resultado = new HashMap<>();

		String user = "areports";
		String host = "137.184.81.141";
		String[] privateKeys = { 
				"C:\\Users\\Mitzi Alicia Moreno\\.ssh\\areports_ed25519",
				"C:\\Users\\Isabel Loyo Luna\\.ssh\\areports_ed25519",
				"C:\\Users\\Marisol Cruz Maderos\\.ssh\\areports_ed25519" 
				};

		String userHome = System.getProperty("user.home");
		String baseLocal = userHome + File.separator + "Downloads" + File.separator;

		try {
			SSHClient ssh = new SSHClient();
			ssh.addHostKeyVerifier(new net.schmizz.sshj.transport.verification.PromiscuousVerifier());

			ssh.connect(host);
			boolean autenticado = false;

			for (String keyPath : privateKeys) {
				try {
//					System.out.println("[INFO] Intentando clave: " + keyPath);

					ssh.authPublickey(user, ssh.loadKeys(keyPath));

					System.out.println("[OK] Autenticado con: " + keyPath);
					autenticado = true;
					break;

				} catch (Exception e) {
					System.out.println("[WARNING] Fallo con clave: " + keyPath);
				}
			}

			if (!autenticado) {
				throw new RuntimeException("No se pudo autenticar con ninguna clave");
			}

			System.out.println("[OK] Conexion exitosa");

			SFTPClient sftp = ssh.newSFTPClient();

			String[] remotePaths = { "gepp_storage/USA/Jiki/LM/" + fecha + "/",
					"gepp_storage/CANADA/Jiki/LM/" + fecha + "/" };

			String systemTemp = System.getProperty("java.io.tmpdir");
			Path baseTempDir = Paths.get(systemTemp, "DownloadLMs");

			if (!Files.exists(baseTempDir)) {
				Files.createDirectories(baseTempDir);
			}

//			System.out.println("[INFO] Temp base: " + baseTempDir);

			for (String remotePath : remotePaths) {

				if (Thread.currentThread().isInterrupted()) {
					System.out.println("[STOP] Proceso detenido por el usuario");
					return resultado;
				}

				String region;
				String zipName;

				if (remotePath.contains("USA")) {
					zipName = "LMs_USA_" + fecha + ".zip";
					region = "USA";
				} else {
					zipName = "LMs_CANADA_" + fecha + ".zip";
					region = "CANADA";
				}

				System.out.println("[INFO] Descargando archivos de " + region + " desde la ruta: " + remotePath);

				try {
					sftp.stat(remotePath);
				} catch (Exception e) {
					System.out.println("[WARNING] La ruta no existe: " + remotePath);
					e.printStackTrace();
					continue;
				}

				List<RemoteResourceInfo> files = sftp.ls(remotePath);

				long totalGlobal = files.stream()
						.filter(f -> !f.isDirectory() && !f.getName().equals(".") && !f.getName().equals(".."))
						.mapToLong(f -> f.getAttributes().getSize()).sum();

				long descargadoGlobal = 0;

				String zipPath = baseLocal + zipName;
				Path tempDir = baseTempDir.resolve(region);

				if (!Files.exists(tempDir)) {
					Files.createDirectories(tempDir);
				}

//				System.out.println("[INFO] Descargando en temp: " + tempDir);
				System.out.println("[Downloading...]");

				// descarga
				for (RemoteResourceInfo file : files) {

					if (Thread.currentThread().isInterrupted()) {
						System.out.println("[STOP] Descarga detenida por usuario");
						return resultado;
					}

					String name = file.getName();

					if (name.equals(".") || name.equals("..") || file.isDirectory())
						continue;

					String remoteFile = remotePath + name;
					Path localFile = tempDir.resolve(name);

//					System.out.println("[Downloading...] " + name);

					try (InputStream is = sftp.open(remoteFile).new RemoteFileInputStream();
							OutputStream os = new BufferedOutputStream(new FileOutputStream(localFile.toFile()),
									2097152)) {

						byte[] buffer = new byte[10_000_000];
						int len;

						try {
							long totalBytes = file.getAttributes().getSize();
							long bytesLeidos = 0;

							while ((len = is.read(buffer)) != -1) {

								if (Thread.currentThread().isInterrupted()) {
									System.out.println("[STOP] Descarga interrumpida");
									return resultado;
								}

								os.write(buffer, 0, len);

								// Progreso
								bytesLeidos += len;
								descargadoGlobal += len;

								if (listener != null && totalGlobal > 0) {
									listener.onProgress(name, descargadoGlobal, totalGlobal);
								}

							}

						} catch (IOException ex) {

							if (Thread.currentThread().isInterrupted()) {
								System.out.println("[STOP] Proceso interrumpido por usuario");
								return resultado;
							}

							throw ex;
						}

					} catch (Exception e) {

						if (Thread.currentThread().isInterrupted()) {
							System.out.println("[INFO] Descarga cancelada correctamente.");
							return resultado;
						}

						System.out.println("[ERROR] Error real descargando archivo:");
						e.printStackTrace();
					}
				}

				System.out.println("[OK] Descarga completa: " + region);

				// ZIP
//				System.out.println("[INFO] Creando ZIP: " + zipName);

				try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {

					zos.setLevel(Deflater.BEST_SPEED);

					for (Path path : (Iterable<Path>) Files.walk(tempDir)::iterator) {

						if (Thread.currentThread().isInterrupted()) {
							System.out.println("[STOP] ZIP cancelado");
							return resultado;
						}

						if (!Files.isDirectory(path)) {

							zos.putNextEntry(new ZipEntry(path.getFileName().toString()));

							try (InputStream fis = new BufferedInputStream(new FileInputStream(path.toFile()),
									2097152)) {

								byte[] buffer = new byte[10_000_000];
								int len;

								while ((len = fis.read(buffer)) != -1) {

									if (Thread.currentThread().isInterrupted()) {
										System.out.println("[STOP] ZIP interrumpido");
										return resultado;
									}

									zos.write(buffer, 0, len);
								}
							}

							zos.closeEntry();
						}
					}
				}

				System.out.println("[OK] ZIP creado: " + zipPath);

				// LIMPIAR TEMP
				Files.walk(tempDir).sorted(Comparator.reverseOrder()).forEach(p -> {
					try {
						Files.delete(p);
					} catch (Exception ignored) {
					}
				});

//				System.out.println("[OK] Temp eliminado: " + region);

				resultado.put(region, zipPath);
			}

			sftp.close();
			ssh.disconnect();

			System.out.println("[OK] Proceso terminado");

		} catch (Exception e) {
			if (Thread.currentThread().isInterrupted()
					|| (e.getCause() != null && e.getCause() instanceof InterruptedException)) {

				System.out.println("[INFO] Proceso cancelado correctamente");
			} else {
				System.out.println("[ERROR] Error real:");
				e.printStackTrace();
			}
		}

		return resultado;
	}
}