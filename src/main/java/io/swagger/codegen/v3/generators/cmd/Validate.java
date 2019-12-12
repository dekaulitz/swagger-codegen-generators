package io.swagger.codegen.v3.generators.cmd;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.AuthorizationValue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>Validate class.</p>
 *
 * @author ovo
 * @version $Id: $Id
 */
public class Validate implements Runnable {

    private String spec;

    /**
     * <p>Setter for the field <code>spec</code>.</p>
     *
     * @param spec a {@link java.lang.String} object.
     */
    public void setSpec(String spec) {
        this.spec = spec;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        System.out.println("Validating spec file (" + spec + ")");

        OpenAPIV3Parser parser = new OpenAPIV3Parser();;
        List<String> messageList = parser.readWithInfo(spec, (List<AuthorizationValue>) null).getMessages();
        Set<String> messages = new HashSet<String>(messageList);

        for (String message : messages) {
            System.out.println(message);
        }

        if (messages.size() > 0) {
            throw new ValidateException();
        }
    }
}
