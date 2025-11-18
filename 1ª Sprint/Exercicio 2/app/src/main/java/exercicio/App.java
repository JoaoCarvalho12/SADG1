package exercicio;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class App {

    // --- CLASSE AUXILIAR PARA GUARDAR O MELHOR RESULTADO ---
    private static class DecryptionResult {
        int d1, d3, d2;
        String mainMessageDecrypted;
        String salt1Decrypted;
        String salt2Decrypted;
        int score;

        public DecryptionResult(int d1, int d3, int d2, String msg, String s1, String s2, int score) {
            this.d1 = d1;
            this.d3 = d3;
            this.d2 = d2;
            this.mainMessageDecrypted = msg;
            this.salt1Decrypted = s1;
            this.salt2Decrypted = s2;
            this.score = score;
        }
    }

    // --- HEURÍSTICA DE VALIDAÇÃO FORTIFICADA (Mais Peso nos Termos-Chave) ---
    private static final Set<String> COMMON_SEQUENCES = new HashSet<>(Set.of(
        // Termos de Alto Valor (Tópicos do Curso)
        "CRIPTOGRAFIA", "ALGORITMOS", "PROGRAMACA", "SEGURANCA", "DESLOCAMENTO", 
        "SISTEMA", "TESTE", "CODIGO", "CESAR", "ASSINATURA", "HISTORICOS", 
        // Bigramas e Trigramas Comuns em Português
        "DE", "DO", "DA", "ES", "AR", "EM", "NA", "OS", "AS", "RA", "ER", 
        "QUE", "CAO", "NTO", "ICO", "IS", "AM", "MENTO", "ESSA", "PARA", 
        "TE", "TI", "TO", "LHA", "PRO", "GRA", "MOS", "RIT", "SEC"
    ));

    /**
     * Decifra uma String usando a Cifra de César com o deslocamento (shift) fornecido.
     */
    public static String decryptCaesar(String ciphertext, int shift) {
        StringBuilder decryptedText = new StringBuilder();
        int effectiveShift = 26 - (shift % 26); 
        
        for (char character : ciphertext.toUpperCase().toCharArray()) {
            if (character >= 'A' && character <= 'Z') {
                char decryptedChar = (char) (((character - 'A' + effectiveShift) % 26) + 'A');
                decryptedText.append(decryptedChar);
            } else {
                decryptedText.append(character);
            }
        }
        return decryptedText.toString();
    }

    /**
     * Calcula o Score de Coerência (número de sequências comuns encontradas).
     */
    public static int calculateScore(String text) {
        int score = 0;
        for (String seq : COMMON_SEQUENCES) {
            if (text.contains(seq)) {
                score++;
            }
        }
        return score;
    }

    /**
     * Executa a Força Bruta e seleciona a solução com o Score de Coerência mais alto.
     */
    public static void crackCipher(String name, String fullCiphertext) {
        
        fullCiphertext = fullCiphertext.toUpperCase().replaceAll("[^A-Z]", "");
        String nomeCompleto = name.toUpperCase();
        
        if (fullCiphertext.length() < 6) {
            System.out.printf("%-20s | Nenhuma solução válida encontrada.\n", nomeCompleto);
            return;
        }

        String salt1Cipher = fullCiphertext.substring(0, 3);
        String salt2Cipher = fullCiphertext.substring(fullCiphertext.length() - 3);
        String mainMessageCipher = fullCiphertext.substring(3, fullCiphertext.length() - 3);
        
        DecryptionResult bestResult = null;
        // Limite mínimo de 4 garante que palavras curtas de baixa coerência são eliminadas.
        int minScoreThreshold = 4; 
        
        for (int d1 = 1; d1 <= 25; d1++) { 
            String salt1Decrypted = decryptCaesar(salt1Cipher, d1);

            for (int d2 = 1; d2 <= 25; d2++) {
                String salt2Decrypted = decryptCaesar(salt2Cipher, d2);
                
                for (int d3 = 1; d3 <= 25; d3++) {
                    String mainMessageDecrypted = decryptCaesar(mainMessageCipher, d3);
                    int currentScore = calculateScore(mainMessageDecrypted);

                    // Seleciona o resultado se atingir o mínimo E se for melhor que o atual.
                    if (currentScore >= minScoreThreshold) {
                         if (bestResult == null || currentScore > bestResult.score) {
                            bestResult = new DecryptionResult(
                                d1, d3, d2, mainMessageDecrypted, salt1Decrypted, salt2Decrypted, currentScore
                            );
                        }
                    }
                }
            }
        }
        
        if (bestResult != null) {
            // Imprime o melhor resultado encontrado
            System.out.printf("%-20s | (%-2d, %-2d, %-2d)    | %-20s | %s/%s\n",
                              nomeCompleto, bestResult.d1, bestResult.d3, bestResult.d2, bestResult.mainMessageDecrypted, bestResult.salt1Decrypted, bestResult.salt2Decrypted);
        } else {
            System.out.printf("%-20s | Nenhuma solução válida encontrada.\n", nomeCompleto);
        }
    }

    public static void main(String[] args) {
        // Nomes normalizados (sem acentos/cedilhas) no formato "NOME PROPRIO ULTIMOSOBRENOME"
        Map<String, String> hashData = new HashMap<>();
        hashData.put("PEDRO MORGADO", "RACVRURXWJARXZXP");
        hashData.put("DANIEL PEREIRA", "FPGRAXRADIJGXHBDRYK");
        hashData.put("VASCO GONCALVES", "PWNOBZONEQRVEBENY");
        hashData.put("EMANUEL MAIA", "MIBOVOMDBSMSNKNOWMY"); 
        hashData.put("JOAO CARVALHO", "PZPVEWTEHMRLEEOT");
        hashData.put("JOEL SA", "VPAFRPHUFLDORXX");
        hashData.put("SERGIO PEREIRA", "QZSWHUURULVWDZSH");
        hashData.put("ANDRE OLIVEIRA", "NHQRGXEIDVGPUXPTIM");
        hashData.put("DANIEL SOUSA", "HXJZWQSBQWOHIFOMEM");
        hashData.put("DAVID SOBRAL", "EYPCUIJHQTEHKS");
        hashData.put("EMANUEL SILVA", "EMHNBRVJOBNXV"); 
        hashData.put("FRANCISCO ROCHA", "DLJIAAWKQIKIWIIY");
        hashData.put("FRANCISCO SILVA", "YAXODCNKXULLG");
        hashData.put("LEANDRO MONTEIRO", "KTZLKHEJAOEWOIA");
        hashData.put("PAULO MARMELO", "BKPZIDBHVODXJULX");
        hashData.put("PEDRO PEREIRA", "TENBPABTFJTGVBF");
        hashData.put("SANDRO FERREIRA", "MONGENONYUBFBCKO");
        
        System.out.println("Resultados da Desencriptação da Cifra de César:");
        System.out.println("==========================================================================================================");
        System.out.println("NOME E SOBRENOME      | DESLOCAMENTOS | MENSAGEM PRINCIPAL   | SALTS (S1/S2)");
        System.out.println("----------------------|---------------|----------------------|-------------------");
        
        for (Map.Entry<String, String> entry : hashData.entrySet()) {
            crackCipher(entry.getKey(), entry.getValue());
        }
    }
}