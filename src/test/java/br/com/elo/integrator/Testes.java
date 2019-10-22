package br.com.elo.integrator;

public class Testes {
    public static void main(String[] args) {

        try {
            System.out.println("indo");
            new Testes().teste();
            System.out.println("foi");

        } catch (Throwable e ) {
            System.out.println("errorr");
        }
    }

    public void teste() {
        throw new NoSuchFieldError();
    }
}
