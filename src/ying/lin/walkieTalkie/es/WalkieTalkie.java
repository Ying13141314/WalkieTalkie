package ying.lin.walkieTalkie.es;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Clase que contiene todos las funcionalidades.
 */
public class WalkieTalkie {

    /**
     * Scanner para recoger información que escribe el usuario.
     */
    private Scanner miScanner;
    /**
     * Variable que guarda el puerto del receptor.
     */
    private int puertoReceptor;
    /**
     * Variable que guarda el ip del receptor.
     */
    private String ip;

    /**
     * Creamos el servicio UDP en el estado.
     */
    DatagramSocket miDatagramSocket;

    /**
     * Controlador
     * @param puerto
     * @throws SocketException
     */
    public WalkieTalkie(int puerto) throws SocketException {
        miScanner = new Scanner(System.in);
        miDatagramSocket = new DatagramSocket(puerto);
    }

    /**
     * Método principal que controla y recoge las informaciones proporcionado por el usuario.
     * @throws IOException
     */
    public void menu() throws IOException {
        int opcion;
        boolean continuar = true;
        boolean comprobacion = false;
        // Pregunta donde se tiene que enviar
        System.out.println("Escriba el puerto del walkietalkie receptor");

        puertoReceptor = validacionNumero();

        //Comprobar si el host introducido es valido o no.
        while (!comprobacion) {
            System.out.println("Introduce el ip que quieres conectar.");
            ip = miScanner.nextLine();
            if (validacionHost(ip)) {
                comprobacion = validacionHost(ip);
            } else {
                System.err.println("Por favor introduzca un host válido");
            }
        }

        while (continuar) {
            System.out.println("1.Enviar mensaje");
            System.out.println("2.Recibir mensaje");
            System.out.println("Elige una de las opciones");
            opcion = validacionNumero();

            switch (opcion) {
                case 1:
                    enviarComunicacion();
                    continuar = false;
                    break;
                case 2:
                    recibirComunicacion();
                    continuar = false;
                    break;
                default:
                    System.out.println("Solo puedes introducir 1 y 2");
                    continuar = true;
            }
        }

        //Cerramos UDP.
        miDatagramSocket.close();
    }

    /**
     * Método que valida el ip introducido por el usuario sea lo correcto.
     * @param ip
     * @return
     */
    public boolean validacionHost(String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

        return ip.matches(PATTERN);
    }

    /**
     * Método que valida los números introducido por el usuario sea número y no string.
     * @return
     */
    private int validacionNumero() {
        boolean comprobar = false;
        int numero = 0;
        while (!comprobar) {
            try {
                numero = Integer.parseInt(miScanner.nextLine());
                comprobar = true;
            } catch (Exception e) {
                System.err.println("Debes introducir un número");
            }
        }
        return numero;
    }

    /**
     * Método que pida al usuario escriba los mensajes y enviarlo a otro usuario.
     * @return si usuario escribe cambio y corto o no.
     * @throws IOException
     */
    private boolean enviar() throws IOException {

        DatagramPacket paqueteInformacion = null;
        System.out.println("Escribe las información que quieres enviar, si quieres finalizar la converzación escribe cambio y corto");

        //Preparamos los datos que queremos enviar.
        ArrayList<String> datos = new ArrayList<>();

        String dato = null;
        boolean isCambio = false;

        while (!isCambio) {
            dato = miScanner.nextLine();
            datos.add(dato);

            isCambio = dato.equalsIgnoreCase("cambio") || dato.equalsIgnoreCase("cambio y corto");
        }

        for (String miDatos : datos) {
            //Creamos el paquete.
            paqueteInformacion = new DatagramPacket(miDatos.getBytes(),
                    miDatos.getBytes().length, InetAddress.getByName(ip), puertoReceptor);

            //Y lo enviamos
            miDatagramSocket.send(paqueteInformacion);
        }

        return dato.equalsIgnoreCase("cambio y corto");
    }

    /**
     * Método que recibe las informaciones escrito por el otro usuario.
     * Si se tarda más de un cierto tiempo en recibir un mensaje, se le muestra de nuevo el menú.
     * @return si usuario recibe cambio y corto o no.
     * @throws IOException
     */
    private boolean recibir() throws IOException {

        boolean isCambio = false;
        String dato = null;

        //Preparamos el paquete de información vacio para recibir información.
        byte[] datos = new byte[1024];
        DatagramPacket recibirPaqueteInformacion = new DatagramPacket(datos, datos.length);
        System.out.println("Recibiendo mensaje");

        //Controlar el tiempo de espera para que no estén los dos esperando infinitamente.
        miDatagramSocket.setSoTimeout(30000);

        while (!isCambio) {
            //Recibe las informaciones.
            try {
                miDatagramSocket.receive(recibirPaqueteInformacion);
                dato = new String(recibirPaqueteInformacion.getData(), 0, recibirPaqueteInformacion.getLength());

                isCambio = dato.equalsIgnoreCase("cambio") || dato.equalsIgnoreCase("cambio y corto");
                // Imprimimos por pantalla el mensaje recibido
                System.out.println(dato);
            } catch (Exception e) {
                System.out.println("Parece que nadie te has hablado intentalo más tarde o hablas tu");
                menu();
            }

        }
        return dato.equalsIgnoreCase("cambio y corto");
    }

    /**
     * Método que controla la comynicacion entre ambos usuarios.(Empieza enviando).
     * @throws IOException
     */
    public void enviarComunicacion() throws IOException {
        boolean dejarHablar = false;

        while (!dejarHablar) {
            if (enviar()) break;
            dejarHablar = recibir();
        }
    }

    /**
     * Método que controla la comynicacion entre ambos usuarios.(Empieza recibiendo).
     * @throws IOException
     */
    public void recibirComunicacion() throws IOException {
        boolean dejarRecibir = false;

        while (!dejarRecibir) {
            if (recibir()) break;
            dejarRecibir = enviar();
        }
    }

}
