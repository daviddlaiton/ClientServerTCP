package Logica;

import Interfaz.InterfazCliente;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Cliente extends Thread {

    public final static int PUERTO = 18798;
    public final static String SERVER = "localhost";
    private static File archivo;
    public final static String repositorio = ".\\ClientRepository\\";
    public static boolean continuar = true;
    public static boolean termino = false;
    public static boolean sinError = true;
    public final static int MAX_TAM = 300022386;
    private static Socket sock;
    private InterfazCliente interfaz;
    private static DataInputStream dis = null;
    private static DataOutputStream dos = null;
    private boolean finConexion = false;
    public String darNombreServidor() {
        return SERVER + ":" + PUERTO;
    }

    public Cliente(InterfazCliente interfaz) {
        this.interfaz = interfaz;

    }

    public void conectar() throws IOException {
        sock = new Socket(SERVER, PUERTO);
        dos = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
        dis = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
    }

    public void setFile(File file) {
        archivo = file;

    }

    public void run() {
        interfaz.habilitarBotonIniciar(false);
        interfaz.habilitarBotonDetener(true);

        sinError = transferir();
        if (continuar && sinError) {
            interfaz.cambiarEstadoConexionATerminado();
        } else if (continuar && !sinError) {
            interfaz.cambiarEstadoConexionAError();
        }
        if(!finConexion)
        interfaz.habilitarBotonIniciar(true);
        interfaz.habilitarBotonDetener(false);
        
        continuar = true;
    }

    public boolean transferir() {
        if (archivo != null) {

            FileOutputStream fos = null;
            try {
                sleep(1000);

                dos = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
                dis = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
                int tamanio = 0;

                try {
                    dos.writeUTF(archivo.getAbsolutePath());
                    dos.flush();
                    tamanio = dis.readInt();
                } catch (Exception e) {
                    interfaz.notificarCierredeConexio();
                    continuar = false;
                    finConexion = true;
                    return false;
                }

// Se recibe el archivo
                byte[] byteDestino = new byte[MAX_TAM];
                File nuevoArch = new File(repositorio + archivo.getName());
                fos = new FileOutputStream(nuevoArch);
                int n = 0;
                while (tamanio > 0 && (n = dis.read(byteDestino, 0, (int) Math.min(byteDestino.length, tamanio))) != -1) {
                    fos.write(byteDestino, 0, n);
                    System.out.println("Paquete recibido: " + n);
                    tamanio -= n;
                }
                if (!continuar) {
                    fos.close();
                    nuevoArch.delete();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;

            } finally {
                if (fos != null && continuar) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        return false;
                    }
                }

            }

        }
        return false;
    }

}
