/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.prompt;

import org.jline.reader.LineReader;
import org.springframework.util.StringUtils;

/**
 *
 * @author jhay
 */
public class InputReader {
    public static final Character DEFAULT_MASK = '*';

    private Character mask;
    private LineReader lineReader;

    ShellHelper shellHelper;

    public InputReader(LineReader lineReader, ShellHelper shellHelper) {
        this(lineReader, shellHelper, null);
    }
    public InputReader(LineReader lineReader, ShellHelper shellHelper, Character mask) {
        this.lineReader = lineReader;
        this.shellHelper = shellHelper;
        this.mask = mask != null ? mask : DEFAULT_MASK;
    }

    public String prompt(String  prompt) {
        return prompt(prompt, null, true);
    }

    public String prompt(String  prompt, String defaultValue) {
        return prompt(prompt, defaultValue, true);
    }

    public String prompt(String  prompt, String defaultValue, boolean echo) {
        String answer = "";
        if (echo) {
            answer = lineReader.readLine(prompt + ": ");
        } else {
            answer = lineReader.readLine(prompt + ": ", mask);
        }
        if (StringUtils.isEmpty(answer)) {
            return defaultValue;
        }
        return answer;
    }
}
