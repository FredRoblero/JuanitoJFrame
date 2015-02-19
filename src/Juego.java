
/**
 * Juego
 *
 * ????????????????????????????????????
 *
 * @author Fred Roblero Maldonado A01037070
 * @author Iván Alejandro Leal Cervantes A00815154
 * @version 1.0
 * @date ?2&11&2015
 */
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.LinkedList;
import javax.swing.JFrame;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author AntonioM
 */
public class Juego extends JFrame implements Runnable, KeyListener {

    private final int iMAXANCHO = 10; // maximo numero de personajes por ancho
    private final int iMAXALTO = 8;  // maxuimo numero de personajes por alto
    private Base basPrincipal;         // Objeto principal
    private Base basMalo;         // Objeto malo
    private LinkedList<Base> lklChimpys; //Lista de objetos Chimpys
    private LinkedList<Base> lklDiddys; //Lista de objetos Diddys

    /* objetos para manejar el buffer del Applet y este no parpadee */
    private Image imaImagenApplet;   // Imagen a proyectar en Applet	
    private Graphics graGraficaApplet;  // Objeto grafico de la Imagen
    private AudioClip adcSonidoChimpy;   // Objeto sonido de Chimpy
    private AudioClip adcSonidoDiddy;   // Objeto sonido de Chimpy
    private int iPosY; // Variable int posicion en Y
    private int iPosX; //Variable int posicion en X
    private boolean bPausa; //Booleana de Pausa
    private boolean bGameover; //Booleana de Gameover
    private int iVelocidad; //Variable Velocidad
    private int iVidas; //variable de Vidas
    private int iPuntos; //varialbe de puntos

    public Juego() { //Juego 
        // hago el applet de un tamaño 500,500
        setSize(800, 500);
        iPosY = (getHeight() / (iMAXALTO + 1)); //Darle valor a Y
        iPosX = (getWidth() / (iMAXANCHO + 1)); //Darle valor a X
        iPuntos = 0; //Inicia en 0 los puntos
        iVidas = ((int) (Math.random() * 2) + 4);
        iVelocidad = 1; //Dar valor a Velocidad

        //Booleanas que manipulan el juego
        bPausa = false;
        bGameover = false;

        URL urlImagenPrincipal = this.getClass().getResource("juanito.gif");

        // se crea el objeto para principal 
        basPrincipal = new Base(0, 0, getWidth() / iMAXANCHO,
                getHeight() / iMAXALTO,
                Toolkit.getDefaultToolkit().getImage(urlImagenPrincipal));

        // se posiciona a principal  en la esquina superior izquierda del Applet 
        basPrincipal.setX(getWidth() / 2);
        basPrincipal.setY(getHeight() / 2);

        // defino la imagen del malo
        URL urlImagenMalo = this.getClass().getResource("chimpy.gif");
        URL urlImagenBueno = this.getClass().getResource("diddy.gif");

        // se crea el objeto para malo 
        int iPosX = (iMAXANCHO - 1) * getWidth() / iMAXANCHO;
        int iPosY = (iMAXALTO - 1) * getHeight() / iMAXALTO;

        //Inicializo la Linked List de Chimpys
        lklChimpys = new LinkedList<Base>();
        
        // genero un numero azar de 3 a 5
        int iAzar = (int) (Math.random() * 3) + 3;

        for (int iI = 0; iI < iAzar; iI++) {

            basMalo = new Base(100, 100 + iI * 30, getWidth() / iMAXANCHO,
                    getHeight() / iMAXALTO,
                    Toolkit.getDefaultToolkit().getImage(urlImagenMalo));

            //Reposiciono al Mono
            basMalo.setY(((int) (Math.random() * 7) + 1) * basMalo.getAlto());
            basMalo.setX(((int) (Math.random() * 9) + 1) * basMalo.getAncho());

            lklChimpys.add(basMalo);
        }

        //INicializo linked list de DIDDYS
        lklDiddys = new LinkedList<Base>();

        for (int iI = 0; iI < iAzar; iI++) {

            basMalo = new Base(iPosX, iPosY, getWidth() / iMAXANCHO,
                    getHeight() / iMAXALTO,
                    Toolkit.getDefaultToolkit().getImage(urlImagenBueno));

            //Reposiciono al Mono
            basMalo.setY(((int) (Math.random() * 7) + 1) * basMalo.getAlto());
            basMalo.setX(((int) (Math.random() * 9) + 1) * basMalo.getAncho());
            lklDiddys.add(basMalo);
        }

        URL urlSonidoChimpy = this.getClass().getResource("monkey2.wav");
        //adcSonidoChimpy = getAudioClip (urlSonidoChimpy);
        //adcSonidoChimpy.play();

        addKeyListener(this); //Añade Key Listener

        // Declaras un hilo
        Thread th = new Thread(this);
        // Empieza el hilo
        th.start();

    }

    /**
     * run
     *
     * Metodo sobrescrito de la clase <code>Thread</code>.<P>
     * En este metodo se ejecuta el hilo, que contendrá las instrucciones de
     * nuestro juego.
     *
     */
    public void run() {
        /* mientras dure el juego, se actualizan posiciones de jugadores
         se checa si hubo colisiones para desaparecer jugadores o corregir
         movimientos y se vuelve a pintar todo
         */
        while (!bGameover) {//Se detiene si es Gameover
            if (!bPausa) {//Se detiene si se pausa
                actualiza();
                checaColision();
            }
            repaint();
            try {
                // El thread se duerme.
                Thread.sleep(20);
            } catch (InterruptedException iexError) {
                System.out.println("Hubo un error en el juego "
                        + iexError.toString());
            }
        }
    }

    /**
     * actualiza
     *
     * Metodo que actualiza la posicion de los objetos
     *
     */
    public void actualiza() {

        for (Base basMalo : lklChimpys) { //Mover a los Chimpys
            basMalo.setX(basMalo.getX() - iVelocidad);

        }

        for (Base basMalo : lklDiddys) { //Mover a los Diddys
            basMalo.setX(basMalo.getX() + iVelocidad);

        }

        if (iVidas == 0) { //si no hay vidas
            bGameover = true;//GameOver
        }

    }

    /**
     * checaColision
     *
     * Metodo usado para checar la colision entre objetos
     *
     */
    public void checaColision() {

        if (basPrincipal.getY() < 0) { // y esta pasando el limite
            //regresa el Principal a una posicion anterior
            basPrincipal.setY(0);
        } // y se esta saliendo del applet
        else if (basPrincipal.getY() + basPrincipal.getAlto()
                > getHeight()) {
            //regresa el Principal a una posicion anterior
            basPrincipal.setY(getHeight() - basPrincipal.getAlto());

        } else if (basPrincipal.getX() < 0) { // y se sale del applet
            //regresa el Principal a una posicion anterior
            basPrincipal.setX(0);
        } else if (basPrincipal.getX() + basPrincipal.getAncho()
                > getWidth()) {
            //regresa el Principal a una posicion anterior
            basPrincipal.setX(getWidth() - basPrincipal.getAncho());
        }

        for (Base basMalo : lklChimpys) { //Colision con Chimpys

            if (basMalo.getX() < 0) {//Si colisiona con pared
                basMalo.setY(((int) (Math.random() * 7) + 1)
                        * basMalo.getAlto());
                basMalo.setX(getWidth() - basMalo.getAncho());
            } else if (basPrincipal.intersecta(basMalo)) {//si hay interseccion
                //adcSonidoChimpy.play();//Sonido
                iPuntos += 10;//añade 10 puntos
                //Regresa al chimpy
                basMalo.setY(((int) (Math.random() * 7) + 1)
                        * basMalo.getAlto());
                basMalo.setX(getWidth() - basMalo.getAncho());
                //adcSonidoChimpy.play();
            }
        }
        for (Base basMalo : lklDiddys) { //Colision con diddys

            if (basMalo.getX() + basMalo.getAncho() //Si colisona con Pared
                    > getWidth()) {
                basMalo.setX(0);
                basMalo.setY(((int) (Math.random() * 7) + 1) * basMalo.getAlto());

            } else if (basPrincipal.intersecta(basMalo)) {//si hay interseccion
                //adcSonidoChimpy.play();//Sonido
                iVidas--;//Resta una vida
                //Regresa el diddy
                basMalo.setY(((int) (Math.random() * 7) + 1) * basMalo.getAlto());
                basMalo.setX(0);
                iVelocidad++;
            }

        }

    }

    /**
     * update
     *
     * Metodo sobrescrito de la clase <code>Applet</code>, heredado de la clase
     * Container.<P>
     * En este metodo lo que hace es actualizar el contenedor y define cuando
     * usar ahora el paint2
     *
     * @param graGrafico es el <code>objeto grafico</code> usado para dibujar.
     *
     */
    public void paint(Graphics graGrafico) { //UPDATE
        // Inicializan el DoubleBuffer
        if (imaImagenApplet == null) {
            imaImagenApplet = createImage(this.getSize().width,
                    this.getSize().height);
            graGraficaApplet = imaImagenApplet.getGraphics();
        }

        // Actualiza la imagen de fondo.
        URL urlImagenFondo = this.getClass().getResource("ciudad.png");
        Image imaImagenFondo = Toolkit.getDefaultToolkit().getImage(urlImagenFondo);
        graGraficaApplet.drawImage(imaImagenFondo, 0, 0, getWidth(), getHeight(), this);

        // Actualiza el Foreground.
        graGraficaApplet.setColor(getForeground());
        paint2(graGraficaApplet);

        // Dibuja la imagen actualizada
        graGrafico.drawImage(imaImagenApplet, 0, 0, this);
    }

    /**
     * paint2
     *
     * Metodo sobrescrito de la clase <code>Applet</code>, heredado de la clase
     * Container.<P>
     * En este metodo se dibuja la imagen con la posicion actualizada, ademas
     * que cuando la imagen es cargada te despliega una advertencia.
     *
     * @param graDibujo es el objeto de <code>Graphics</code> usado para
     * dibujar.
     *
     */
    public void paint2(Graphics graDibujo) {

        // si la imagen ya se cargo
        if (basPrincipal != null) {
            //Dibuja la imagen de principal en el Applet
            basPrincipal.paint(graDibujo, this);
            //Dibuja la imagen de malo en el Applet
            basMalo.paint(graDibujo, this);

            for (Base basMalo : lklChimpys) {
                basMalo.paint(graDibujo, this);
            }

            for (Base basMalo : lklDiddys) {
                basMalo.paint(graDibujo, this);
            }

            if (bPausa) {
                graDibujo.drawString("PAUSA", getWidth() / 2 - 40,
                        getHeight() / 2);
            }

            if (bGameover) {
                URL urlImagenFondo = this.getClass().getResource("gameover.png");
                Image imaImagenFondo
                        = Toolkit.getDefaultToolkit().getImage(urlImagenFondo);
                graGraficaApplet.drawImage(imaImagenFondo, 0, 0,
                        getWidth(), getHeight(), this);
            }

            graDibujo.drawString("Vidas: " + iVidas, getWidth() / 4 - 140,
                    getHeight() / 4 - 70);
            graDibujo.drawString("Puntos: " + iPuntos, getWidth() / 4 - 140,
                    getHeight() / 4 - 40);

        } // sino se ha cargado se dibuja un mensaje 
        else {
            //Da un mensaje mientras se carga el dibujo	
            graDibujo.drawString("No se cargo la imagen..", 20, 20);
        }
    }

    /**
     * guardarJuego
     *
     * Metodo que guarda todos los datos del juego
     *
     */
    public void guardarJuego() {

        //PrintWriter pwrOut = new PrintWriter(new FileWriter("Archivo.txt"));
        try (PrintWriter pwrOut = new PrintWriter(new FileWriter("Archivo.txt"))) {
            pwrOut.println(bPausa ? 1 : 0);
            pwrOut.println(iVidas);
            pwrOut.println(iPuntos);
            pwrOut.println(iVelocidad);
            pwrOut.println(basPrincipal.getX());
            pwrOut.println(basPrincipal.getY());
            pwrOut.println(lklChimpys.size());
            for (Base basMalo : lklChimpys) {
                pwrOut.println(basMalo.getX());
                pwrOut.println(basMalo.getY());
            }
            pwrOut.println(lklChimpys.size());
            for (Base basMalo : lklDiddys) {
                pwrOut.println(basMalo.getX());
                pwrOut.println(basMalo.getY());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * cargarJuego
     *
     * Metodo que carga los datos de un juego anterior que se haya guardado
     *
     */
    public void cargarJuego() {
        int iaux;
        URL urlImagenMalo = this.getClass().getResource("chimpy.gif");
        URL urlImagenBueno = this.getClass().getResource("diddy.gif");
        BufferedReader buffer;
        try {
//            // Abrimos el archivo
//            FileInputStream fstream = new FileInputStream("Archivo.txt");
//            // Creamos el objeto de entrada
//            DataInputStream entrada = new DataInputStream(fstream);
//            // Creamos el Buffer de Lectura
//            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
            buffer = new BufferedReader(new FileReader("Archivo.txt"));
            String strLinea;
            // Leer el archivo linea por linea
            strLinea = buffer.readLine();
            iaux = Integer.parseInt(strLinea);
            bPausa = (iaux == 1);
            strLinea = buffer.readLine();
            iaux = Integer.parseInt(strLinea);
            iVidas = iaux;
            strLinea = buffer.readLine();
            iaux = Integer.parseInt(strLinea);
            iPuntos = iaux;
            strLinea = buffer.readLine();
            iaux = Integer.parseInt(strLinea);
            iVelocidad = iaux;
            strLinea = buffer.readLine();
            iaux = Integer.parseInt(strLinea);
            basPrincipal.setX(iaux);
            strLinea = buffer.readLine();
            iaux = Integer.parseInt(strLinea);
            basPrincipal.setY(iaux);
            strLinea = buffer.readLine();
            iaux = Integer.parseInt(strLinea);
            lklChimpys.clear();
            for (int iI = 0; iI < iaux; iI++) {
                basMalo = new Base(iPosX, iPosY, getWidth() / iMAXANCHO,
                        getHeight() / iMAXALTO,
                        Toolkit.getDefaultToolkit().getImage(urlImagenMalo));
                lklChimpys.add(basMalo);
            }
            for (Base basMalo : lklChimpys) {
                strLinea = buffer.readLine();
                iaux = Integer.parseInt(strLinea);
                basMalo.setX(iaux);
                strLinea = buffer.readLine();
                iaux = Integer.parseInt(strLinea);
                basMalo.setY(iaux);
            }
            strLinea = buffer.readLine();
            iaux = Integer.parseInt(strLinea);
            lklDiddys.clear();
            for (int iI = 0; iI < iaux; iI++) {
                basMalo = new Base(iPosX, iPosY, getWidth() / iMAXANCHO,
                        getHeight() / iMAXALTO,
                        Toolkit.getDefaultToolkit().getImage(urlImagenBueno));
                lklDiddys.add(basMalo);
            }
            for (Base basMalo : lklDiddys) {
                strLinea = buffer.readLine();
                iaux = Integer.parseInt(strLinea);
                basMalo.setX(iaux);
                strLinea = buffer.readLine();
                iaux = Integer.parseInt(strLinea);
                basMalo.setY(iaux);
            }

            // Cerramos el archivo
            buffer.close();
        } catch (Exception e) { //Catch de excepciones
            System.err.println("Ocurrio un error: " + e.getMessage());
        }
    }

    /**
     * keyPressed
     *
     * Metodo sobrescrito de la interface <code>KeyListener</code>.<P>
     * En este metodo maneja el evento que se genera al dejar presionada alguna
     * tecla.
     *
     * @param keyEvent es el <code>KeyEvent</code> que se genera en al
     * presionar.
     *
     */
    public void keyPressed(KeyEvent keyEvent) {

    }

    /**
     * keyTyped
     *
     * Metodo sobrescrito de la interface <code>KeyListener</code>.<P>
     * En este metodo maneja el evento que se genera al presionar una tecla que
     * no es de accion.
     *
     * @param keyEvent es el <code>KeyEvent</code> que se genera en al
     * presionar.
     *
     */
    public void keyTyped(KeyEvent keyEvent) {
        // no hay codigo pero se debe escribir el metodo
    }

    /**
     * keyReleased Metodo sobrescrito de la interface
     * <code>KeyListener</code>.<P>
     * En este metodo maneja el evento que se genera al soltar la tecla.
     *
     * @param keyEvent es el <code>KeyEvent</code> que se genera en al soltar.
     *
     */
    @SuppressWarnings("empty-statement")
    public void keyReleased(KeyEvent keyEvent) {
        // no hay codigo pero se debe escribir el metodo
        // si presiono flecha para arriba
        if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
            if (!bGameover && !bPausa) {
                basPrincipal.setY(basPrincipal.getY() - iPosY);
            }
        } // si presiono flecha para abajo
        else if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!bGameover && !bPausa) {
                basPrincipal.setY(basPrincipal.getY() + iPosY);
            }
        } // si presiono flecha a la izquierda
        else if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT) {
            if (!bGameover && !bPausa) {
                basPrincipal.setX(basPrincipal.getX() - iPosX);
            }
        } // si presiono flecha a la derecha
        else if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (!bGameover && !bPausa) {
                basPrincipal.setX(basPrincipal.getX() + iPosX);
            }
        } //Pregunto si el jugador activo o desactivo la pausa
        else if (keyEvent.getKeyCode() == KeyEvent.VK_P) {
            bPausa = !bPausa;
        } //Pregutno si el jugador quiere detener el juego
        else if (keyEvent.getKeyCode() == keyEvent.VK_ESCAPE) {
            bGameover = true;
        } // Pregunto si el jugador quiere guardar el juego
        else if (keyEvent.getKeyCode() == keyEvent.VK_G) {
            guardarJuego();
        } else if (keyEvent.getKeyCode() == keyEvent.VK_C) {
            cargarJuego();
        }
    }

    public static void main(String[] args) {
        Juego Jframejuego = new Juego();
        Jframejuego.setVisible(true);
        Jframejuego.setTitle("JFrame");
        Jframejuego.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Jframejuego.setSize(800, 500);
    }

}
