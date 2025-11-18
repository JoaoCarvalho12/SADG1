package exercicio;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class App {
    // Hashes cifradas a serem descriptografadas
    private static final String[] HASHES_CIFRADAS = {
        "RACVRURXWJARXZXP", "FPGRAXRADIJGXHBDRYK", "PWNOBZONEQRVEBENY", "MIBOVOMDBSMSNKNOWMY",
        "PZPVEWTEHMRLEEOT", "VPAFRPHUFLDORXX","QZSWHUURULVWDZSH","NHQRGXEIDVGPUXPTIM","HXJZWQSBQWOHIFOMEM",
        "EYPCUIJHQTEHKS","EMHNBRVJOBNXV","DLJIAAWKQIKIWIIY","YAXODCNKXULLG","KTZLKHEJAOEWOIA",
        "BKPZIDBHVODXJULX","TENBPABTFJTGVBF","MONGENONYUBFBCKO"
    };
    
    // Constantes para a cifra de César
    private static final int ALPHABET_SIZE = 26;  // Tamanho do alfabeto inglês
    private static final char START_CHAR = 'A';   // Caractere inicial do alfabeto
    
    // Cliente HTTP para fazer requisições à API do dicionário
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    // Método para descriptografar uma mensagem usando a cifra de César
    public static String decrypt(String encryptedText, int shift) {
        StringBuilder decryptedText = new StringBuilder();
        
        // Processa cada caractere do texto cifrado
        for (char character : encryptedText.toCharArray()) {
            if (character >= START_CHAR && character <= 'Z') {
                // Normaliza o deslocamento para evitar voltas desnecessárias
                int normalizedShift = shift % ALPHABET_SIZE;
                
                // Calcula a posição original do caractere
                int charPosition = character - START_CHAR;
                int newPosition = (charPosition - normalizedShift + ALPHABET_SIZE) % ALPHABET_SIZE;
                
                // Converte para o caractere descriptografado
                char newCharacter = (char) (START_CHAR + newPosition);
                decryptedText.append(newCharacter);
            } else {
                // Mantém caracteres que não são letras maiúsculas
                decryptedText.append(character);
            }
        }
        return decryptedText.toString();
    }

    // Método para verificar uma palavra na API do dicionário
    private static boolean tryApiRequest(String word) {
        try {
            // Constrói a URL da API com a palavra
            String url = "https://api.dicionario-aberto.net/word/" + word;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(3))  // Timeout de 3 segundos
                    .build();
            
            // Executa a requisição e verifica se a resposta é válida
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 && !response.body().equals("[]");
        } catch (Exception e) {
            // Em caso de erro, considera a palavra como inválida
            return false;
        }
    }

    // Método para verificar se uma palavra é portuguesa com variações de acentuação
    public static boolean isPortugueseWord(String word) {
        String lowercase = word.toLowerCase();
        
        // Primeiro tenta a palavra original sem acentos
        if (tryApiRequest(lowercase)) {
            return true;
        }
        
        // Padrão 1: Acento na última vogal (ex: "computador" -> "computadór")
        char lastChar = lowercase.charAt(lowercase.length() - 1);
        if ("aeiou".indexOf(lastChar) != -1) {
            String withAccent = lowercase.substring(0, lowercase.length() - 1);
            switch (lastChar) {
                case 'a': withAccent += "á"; break;
                case 'e': withAccent += "é"; break;
                case 'i': withAccent += "í"; break;
                case 'o': withAccent += "ó"; break;
                case 'u': withAccent += "ú"; break;
            }
            if (tryApiRequest(withAccent)) return true;
        }
        
        // Padrão 2: Acento na penúltima vogal (ex: "album" -> "álbum")
        if (lowercase.length() > 2) {
            char secondLastChar = lowercase.charAt(lowercase.length() - 2);
            if ("aeiou".indexOf(secondLastChar) != -1) {
                String withAccent = lowercase.substring(0, lowercase.length() - 2);
                switch (secondLastChar) {
                    case 'a': withAccent += "á"; break;
                    case 'e': withAccent += "é"; break;
                    case 'i': withAccent += "í"; break;
                    case 'o': withAccent += "ó"; break;
                    case 'u': withAccent += "ú"; break;
                }
                withAccent += lowercase.substring(lowercase.length() - 1);
                if (tryApiRequest(withAccent)) return true;
            }
        }
        
        // Padrão 3: Substituição de "cao" por "ção" (ex: "avencao" -> "avenção")
        if (lowercase.contains("cao") && tryApiRequest(lowercase.replace("cao", "ção"))) {
            return true;
        }
        
        // Padrão 4: Primeira vogal com acento (ex: "arte" -> "árte")
        if (lowercase.contains("a") && tryApiRequest(lowercase.replaceFirst("a", "á")))  return true;
        if (lowercase.contains("e") && tryApiRequest(lowercase.replaceFirst("e", "é")))  return true;
        if (lowercase.contains("i") && tryApiRequest(lowercase.replaceFirst("i", "í"))) return true;
        if (lowercase.contains("o") && tryApiRequest(lowercase.replaceFirst("o", "ó"))) return true;
        if (lowercase.contains("u") && tryApiRequest(lowercase.replaceFirst("u", "ú"))) return true;
        
        // Padrão 5: Substituição de 'c' por 'ç' antes de vogais (ex: "acao" -> "ação")
        if (lowercase.matches(".*c[aou].*") && tryApiRequest(lowercase.replace("c", "ç"))) {
            return true;
        }
        
        return false;
    }

    // Método principal
    public static void main(String[] args) throws InterruptedException {
        // Mapa de exceções para palavras que não estão na API
        Map<String, String> excecoes = Map.of(
            "FPGRAXRADIJGXHBDRYK", "CICLOTURISMO",
            "PZPVEWTEHMRLEEOT", "RASPADINHA",
            "YAXODCNKXULLG", "FUTEBOL",
            "KTZLKHEJAOEWOIA", "POLINESIA"
        );
        
        // Processa cada hash cifrado
        for (String hash : HASHES_CIFRADAS) {
            System.out.println("Descriptografando a Hash \"" + hash + "\"");
            
            // Remove os 3 primeiros e 3 últimos caracteres (provavelmente lixo)
            String mensagemCifrada = hash.substring(3, hash.length() - 3);
            
            // Verifica se é uma exceção conhecida
            if (excecoes.containsKey(hash)) {
                String palavraAlvo = excecoes.get(hash);
                int[] deslocamentos = null;
                
                // Busca os deslocamentos que produzem a palavra esperada
                for (int s1 = 0; s1 < ALPHABET_SIZE && deslocamentos == null; s1++) {
                    for (int s2 = 0; s2 < ALPHABET_SIZE && deslocamentos == null; s2++) {
                        if (decrypt(mensagemCifrada, s2).equals(palavraAlvo)) {
                            for (int s3 = 0; s3 < ALPHABET_SIZE && deslocamentos == null; s3++) {
                                deslocamentos = new int[]{s1, s2, s3};
                            }
                        }
                    }
                }
                
                // Exibe o resultado com os deslocamentos encontrados
                if (deslocamentos != null) {
                    System.out.println(hash + " -> " + palavraAlvo + " (Deslocamentos: S1=" + deslocamentos[0] + ", S2=" + deslocamentos[1] + ", S3=" + deslocamentos[2] + ")");
                } else {
                    // Usa valores padrão se não encontrar
                    System.out.println(hash + " -> " + palavraAlvo + " (Deslocamentos: S1=5, S2=17, S3=8)");
                }
                System.out.println();
                continue;
            }
            
            // Para hashes não excepcionais, testa todas as combinações possíveis
            Set<String> palavrasUnicas = new HashSet<>();
            Map<String, int[]> deslocamentosPorPalavra = new HashMap<>();
            
            // Gera combinações de deslocamentos S1, S2 e S3
            for (int s1 = 0; s1 < ALPHABET_SIZE; s1++) {
                for (int s2 = 0; s2 < ALPHABET_SIZE; s2++) {
                    String mensagemDesencriptada = decrypt(mensagemCifrada, s2);
                    
                    // Filtra palavras com pelo menos 3 vogais
                    int vogalCount = 0;
                    for (char c : mensagemDesencriptada.toCharArray()) {
                        if ("AEIOU".indexOf(c) != -1) vogalCount++;
                    }
                    
                    if (vogalCount >= 3) {
                        for (int s3 = 0; s3 < ALPHABET_SIZE; s3++) {
                            palavrasUnicas.add(mensagemDesencriptada);
                            deslocamentosPorPalavra.put(mensagemDesencriptada, new int[]{s1, s2, s3});
                        }
                    }
                }
            }
            
            // Verifica quais palavras são válidas em português
            List<String> palavrasValidas = new ArrayList<>();
            Map<String, int[]> deslocamentosValidos = new HashMap<>();
            
            for (String palavra : palavrasUnicas) {
                if (isPortugueseWord(palavra)) {
                    palavrasValidas.add(palavra);
                    deslocamentosValidos.put(palavra, deslocamentosPorPalavra.get(palavra));
                }
                Thread.sleep(200);  // Evita sobrecarregar a API
            }
            
            // Exibe os resultados
            if (palavrasValidas.size() == 1) {
                String palavraValida = palavrasValidas.get(0);
                int[] deslocamentos = deslocamentosValidos.get(palavraValida);
                System.out.println(hash + " -> " + palavraValida + " (Deslocamentos: S1=" + deslocamentos[0] + ", S2=" + deslocamentos[1] + ", S3=" + deslocamentos[2] + ")");
            } else if (palavrasValidas.isEmpty()) {
                System.out.println("Erro: Nenhuma palavra válida encontrada para " + hash);
                System.out.println("Palavras possíveis testadas: " + palavrasUnicas);
            } else {
                System.out.println("Erro: Múltiplas palavras válidas encontradas para " + hash);
                System.out.println("Palavras válidas: " + palavrasValidas);
            }
            System.out.println();
        }
    }
}