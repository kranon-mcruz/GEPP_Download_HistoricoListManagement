# GEPP_Download_HistoricoListManagement
Aplicación de desarrollada que se conecta a servidor por medio de SFTP, para la descarga de listas de marcación
de una ruta específica.

# Premisas para el funcionamiento
-Acceso a un servidor mediante SSH/SFTP
-Llave válida para autenticación(.shh)
-La llave debe estar en la ruta "C:\Users\<tusuario>\.ssh"
-Ejecutar siguiente comando en terminal de equipo local "sftp -i "/ruta/<nomenclatura de llave ssh>" <usuario de servidor>@<ip de servidor>"
-En clase *DownloadService* hardcodear el user y host en las siguientes variables: String user = ""; y String host = "";
-Definir en la clase *DownloadService* en el siguiente arreglo las rutas donde se ubica la llave .shh: String[] privateKeys = { };.       Correspondientes a los usuarios que tendrán acceso al sistema.
-Conexión a internet estable
-Java 8 o superior instalado
-Contar con contraseña del jar