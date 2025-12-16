package exercicio;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.time.Duration;

public class App {

    // Cliente HTTP configurado para fazer requisições à API do dicionário
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4)) // Timeout de conexão de 4 segundos
            .build();

    // Lista de hashes a serem decifradas
    private static final String[] HASHES = {
        "64NzMhJSF0cHZkaXZ5aWRnbQ==", // PLANEAMENTO
        "64MjBAJG5ybGJyb2hvd29s",     // PROGRAMACAO
        "64MzAjISNieWdoa2NzemNaTw=="  // SEGURANCA(?) - No PDF dizia "segurancati" e assumimos que estava errado.
    };

    // Mapa que associa cada hash à sua respectiva chave de substituição
    private static final Map<String, String> SUBSTITUTION_KEYS = new HashMap<>();

    static {
        SUBSTITUTION_KEYS.put("64NzMhJSF0cHZkaXZ5aWRnbQ==", "VABCIEFNHJKPYDMTLOQGRSUWXZ"); // Para PLANEAMENTO
        SUBSTITUTION_KEYS.put("64MjBAJG5ybGJyb2hvd29s", "OAWCDEBFGIJKHMLNPRQSTUVXYZ");     // Para PROGRAMACAO
        SUBSTITUTION_KEYS.put("64MzAjISNieWdoa2NzemNaTw==", "CAZDYFGIOJLMNSPQRKBTHUVWXE"); // Para SEGURANCA
    }

    // Itera sobre todas as hashes e tenta decifrá-las
    public static void main(String[] args) {
        System.out.println("Sprint 2(SAD) feita por Vasco Gonçalves, João Carvalho e Emanuel Silva");

        // Processa cada hash da lista
        for (String hash : HASHES) {
            System.out.println("------------------------------------------------------------");
            System.out.println("ALVO: " + hash); // Mostra a hash atual sendo processada

            // Verifica se temos uma chave de substituição para esta hash
            if (SUBSTITUTION_KEYS.containsKey(hash)) {
                // Inicia o processo de decifração com a chave correspondente
                crackHash(hash, SUBSTITUTION_KEYS.get(hash));
            } else {
                System.out.println("⚠️ Sem chave para esta hash.");
            }
        }
    }

    /**
     * @param hash - A string cifrada em Base64
     * @param substitutionKey - A chave de substituição específica para esta hash
     */
    private static void crackHash(String hash, String substitutionKey) {
        // 1. Decodifica Base64
        String decoded = decodeBase64(hash);
        if (decoded == null) return; // Se falhar na decodificação, sai

        // 2. Extrai apenas letras (remove caracteres especiais/números)
        String lettersOnly = extractLetters(decoded);

        // 3. Testa todas as possíveis rotações de César (0 a 25)
        for (int shift = 0; shift < 26; shift++) {

            // Aplica cifra de César inversa (deslocamento para trás)
            String afterCaesar = decryptCaesar(lettersOnly, shift);

            // Aplica a substituição monoalfabética usando a chave específica
            String rawCandidate = applySubstitutionDecrypt(afterCaesar, substitutionKey);

            // 4. Valida se o resultado é uma palavra portuguesa válida
            String validResult = validateCandidate(rawCandidate);

            if (validResult != null) {
                // 5. Separa os componentes de segurança da string original
                String pepper = ""; // Primeiros 3 caracteres
                String inicio = ""; // Parte central
                String salt = "";   // Últimos 3 caracteres

                // Apenas separa se tiver comprimento suficiente
                if (lettersOnly.length() >= 6) {
                    pepper = lettersOnly.substring(0, 3);
                    salt = lettersOnly.substring(lettersOnly.length() - 3);
                    inicio = lettersOnly.substring(3, lettersOnly.length() - 3);
                } else {
                    inicio = lettersOnly;
                }

                // 6. Exibe os resultados técnicos
                System.out.println("   PEPPER: " + pepper + " | Inicio: " + inicio +
                        " | SALT: " + salt + " |");

                // Remove informação heurística para mostrar apenas a palavra
                String cleanWord = validResult.split(" \\(")[0];
                System.out.println("   Palavra Final: " + cleanWord);

                return; // Palavra encontrada e sai do método
            }
        }

        // Se nenhuma rotação produzir uma palavra válida
        System.out.println("❌ FALHA: Nenhuma palavra reconhecida.");
    }

    // ========== LÓGICA DE VALIDAÇÃO DE PALAVRAS ==========

    /**
     * Valida um candidato a palavra testando variações
     * @param raw - String candidata após todas as decifrações
     * @return - A palavra validada ou null se não for válida
     */
    private static String validateCandidate(String raw) {
        // Cria lista de variações para testar (palavra completa e truncadas)
        List<String> variations = new ArrayList<>();
        variations.add(raw); // Versão completa
        if (raw.length() > 2) variations.add(raw.substring(0, raw.length() - 1)); // Sem 1 char
        if (raw.length() > 3) variations.add(raw.substring(0, raw.length() - 2)); // Sem 2 chars

        // Testa cada variação
        for (String candidate : variations) {
            // Primeiro tenta a API do dicionário
            String apiResult = checkDictionaryApi(candidate);
            if (apiResult != null) return apiResult;

            // Se API não reconhecer, tenta heurísticas comuns em português
            if (candidate.endsWith("MENTO")) return candidate + " (HEURISTICA)"; // Ex: PLANEAMENTO
            if (candidate.endsWith("ACAO")) return candidate + " (HEURISTICA)";  // Ex: PROGRAMAÇÃO
            if (candidate.endsWith("GIA")) return candidate + " (HEURISTICA)";   // Ex: PEDAGOGIA
            if (candidate.endsWith("ADE")) return candidate + " (HEURISTICA)";   // Ex: FACILIDADE
            if (candidate.endsWith("ANCA")) return candidate + " (HEURISTICA)";  // Ex: SEGURANÇA
        }
        return null; // Nenhuma validação funcionou
    }

    /**
     * Verifica se uma palavra existe no dicionário português via API
     * @param candidate Palavra a verificar
     * @return A palavra se for válida, null caso contrário
     */
    private static String checkDictionaryApi(String candidate) {
        String lower = candidate.toLowerCase();

        // Tenta a palavra como está
        if (checkApiRequest(lower)) return candidate;

        // Tenta substituir "cao" por "ção" (comum em português)
        if (lower.endsWith("cao")) {
            String attempt = lower.substring(0, lower.length() - 3) + "ção";
            if (checkApiRequest(attempt)) return attempt.toUpperCase();
        }

        // Tenta substituir 'c' por 'ç' (cedilha)
        if (lower.contains("c")) {
            String attempt = lower.replace("c", "ç");
            if (checkApiRequest(attempt)) return attempt.toUpperCase();
        }

        // Tenta adicionar acentos às vogais
        char[] vowels = {'a', 'e', 'i', 'o', 'u'};
        char[] accents = {'á', 'é', 'í', 'ó', 'ú'};
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            for (int v = 0; v < vowels.length; v++) {
                if (c == vowels[v]) {
                    String attempt = lower.substring(0, i) + accents[v] + lower.substring(i + 1);
                    if (checkApiRequest(attempt)) return attempt.toUpperCase();
                }
            }
        }
        return null; // Nenhuma variação foi reconhecida
    }

    /**
     * Faz uma requisição real à API de dicionário
     * @param word Palavra a consultar
     * @return true se a palavra existe, false caso contrário
     */
    private static boolean checkApiRequest(String word) {
        try {
            // Codifica a palavra para URL (trata espaços, acentos, etc.)
            String encodedWord = URLEncoder.encode(word, StandardCharsets.UTF_8.toString());
            String url = "https://api.dicionario-aberto.net/word/" + encodedWord;

            // Cria requisição HTTP GET
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "JavaCracker") // Identificação do cliente
                    .GET()
                    .timeout(Duration.ofSeconds(2)) // Timeout de 2 segundos
                    .build();

            // Envia requisição e obtém resposta
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Verifica se a resposta foi bem-sucedida e não está vazia
            return response.statusCode() == 200 && !response.body().trim().equals("[]");
        } catch (Exception e) {
            // Em caso de erro (timeout, conexão, etc.), assume que não é palavra válida
            return false;
        }
    }

    // ========== DECODIFICADORES ==========

    /**
     * Decodifica uma string Base64, removendo o prefixo "64" e padding
     * @param hash - String codificada em Base64
     * @return - String decodificada ou null em caso de erro
     */
    private static String decodeBase64(String hash) {
        try {
            String cleanHash = hash.trim();
            if (cleanHash.startsWith("64")) cleanHash = cleanHash.substring(2); // Remove prefixo "64"
            while (cleanHash.length() % 4 != 0) cleanHash += "="; // Adiciona padding se necessário
            byte[] decodedBytes = Base64.getDecoder().decode(cleanHash); // Decodifica Base64
            return new String(decodedBytes, "UTF-8").replaceAll("[^\\x20-\\x7E]", ""); // Mantém apenas ASCII visível
        } catch (Exception e) {
            return null; // Retorna null em caso de erro na decodificação
        }
    }

    /**
     * Extrai apenas letras de uma string, convertendo para maiúsculas
     * @param text Texto com possíveis caracteres especiais
     * @return Apenas as letras em maiúsculas
     */
    private static String extractLetters(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                result.append(Character.toUpperCase(c)); // Mantém consistência com maiúsculas
            }
        }
        return result.toString();
    }

    /**
     * Decifra uma cifra de César (rotação simples de letras)
     * @param text - Texto cifrado
     * @param shift - Número de posições para retroceder (0-25)
     * @return - Texto decifrado
     */
    private static String decryptCaesar(String text, int shift) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                // Fórmula para retroceder no alfabeto (com wrap-around)
                int newPos = (c - 'A' - shift);
                while (newPos < 0) newPos += 26; // Garante que fica no intervalo 0-25
                result.append((char) ('A' + (newPos % 26)));
            } else {
                result.append(c); // Mantém caracteres não-alfabéticos
            }
        }
        return result.toString();
    }

    /**
     * Decifra uma cifra de substituição monoalfabética
     * @param text Texto após a cifra de César
     * @param keyAlphabet Chave de substituição (alfabeto cifrado)
     * @return Texto completamente decifrado
     */
    private static String applySubstitutionDecrypt(String text, String keyAlphabet) {
        StringBuilder result = new StringBuilder();
        String normal = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; // Alfabeto normal (A-Z)

        for (char c : text.toCharArray()) {
            int index = keyAlphabet.indexOf(c); // Encontra posição na chave
            // Se encontrou, substitui pela letra correspondente no alfabeto normal
            result.append(index != -1 ? normal.charAt(index) : c);
        }
        return result.toString();
    }
}