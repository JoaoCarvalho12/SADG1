package exercicio;

import java.util.*;

public class App {

    
    //Tabela de dados: Nome Completo Simplificado vs. Código Encriptado 1
     
    private static final Map<String, String> DADOS_ALUNOS = Map.ofEntries(
        Map.entry("PEDRO MORGADO", "DSRFC ACFUORC"),
        Map.entry("DANIEL PEREIRA", "NKXSOV ZOBOSBK"),
        Map.entry("VASCO GONCALVES", "XCUEQ IQPECNXGU"),
        Map.entry("EMANUEL MAIA", "LTHUBLS THPH"),
        Map.entry("JOAO CARVALHO", "WBNB PNEINYUB"),
        Map.entry("JOEL SA", "AFVC JR"),
        Map.entry("SERGIO PEREIRA", "ZLYNPV WLYLPYH"),
        Map.entry("ANDRE OLIVEIRA", "FSIWJ TQNAJNWF"),
        Map.entry("DANIEL SOUSA", "QNAVRY FBHFN"),
        Map.entry("DAVID SOBRAL", "OLGTO DZMCLW"),
        Map.entry("EMANUELSILVA", "RZNAHRYFVYIN"), // Corrigido sem espaço para corresponder ao hash
        Map.entry("FRANCISCO ROCHA", "ZLUHWCMWI LIWBU"),
        Map.entry("FRANCISCO SILVA", "BNWJYEOYK OEHRW"),
        Map.entry("LEANDRO MONTEIRO", "XQMZPDA YAZFQUDA"),
        Map.entry("PAULO MARMELO", "NYSJM KYPKCJM"),
        Map.entry("PEDRO PEREIRA", "LAZNK LANAENW"),
        Map.entry("SANDRO FERREIRA", "PXKAOL CBOOBFOX")
    );



    public static String encriptar(String texto, int deslocamento) {
        StringBuilder resultado = new StringBuilder();
        // Normaliza o deslocamento entre 0 e 25
        deslocamento = deslocamento % 26; 

        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);

            if (Character.isLetter(c)) {
                // Assume o caso base (A ou a)
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                
                // Fórmula de Encriptação: (posição + deslocamento) % 26 + base
                char letraCifrada = (char) ((c - base + deslocamento) % 26 + base);
                resultado.append(letraCifrada);
            } else {
                // Mantém espaços e outros caracteres
                resultado.append(c);
            }
        }
        return resultado.toString();
    }

    public static void main(String[] args) {
        System.out.println("--- Análise Criptográfica de Força Bruta (Cifra de César) ---\n");
        
        System.out.printf("%-20s | %-20s | %-15s\n", "CÓDIGO DESENCRIPTADO", "CÓDIGO ENCRIPTADO", "DESLOCAMENTO (K)");
        System.out.println("------------------------------------------------------------------");

        for (Map.Entry<String, String> entry : DADOS_ALUNOS.entrySet()) {
            String nomeClaro = entry.getKey().toUpperCase(); 
            String codigoEsperado = entry.getValue();
            int chaveEncontrada = -1;

            // Tenta todos os 25 deslocamentos (chaves K)
            for (int k = 1; k <= 25; k++) {
                String codigoGerado = encriptar(nomeClaro, k);

                // Compara o código gerado com o código esperado
                if (codigoGerado.equals(codigoEsperado)) {
                    chaveEncontrada = k;
                    break; // Chave encontrada
                }
            }

            if (chaveEncontrada != -1) {
                 System.out.printf("%-20s | %-20s | K = %-13d\n", 
                                  nomeClaro, 
                                  codigoEsperado, 
                                  chaveEncontrada); //Print da Listagem
            } else {
                 System.out.printf("%-20s | %-20s | %-15s\n", 
                                  nomeClaro, 
                                  codigoEsperado, 
                                  "CHAVE NÃO ENCONTRADA"); //Chave não encontrada
            }
        }
    }
}