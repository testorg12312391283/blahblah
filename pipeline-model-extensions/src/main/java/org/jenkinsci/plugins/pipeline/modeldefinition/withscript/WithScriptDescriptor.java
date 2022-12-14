/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.pipeline.modeldefinition.withscript;

import hudson.model.Descriptor;
import org.jenkinsci.plugins.structs.SymbolLookup;
import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.jenkinsci.plugins.workflow.cps.GroovySourceFileAllowlist;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.ExtensionList;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Descriptor for {@link WithScriptDescribable}.
 *
 * @author Andrew Bayer
 */
public abstract class WithScriptDescriptor<T extends WithScriptDescribable<T>> extends Descriptor<T> {

    protected WithScriptDescriptor() {
        ExtensionList.lookupSingleton(WithScriptAllowlist.class).registerScript(getScriptResource());
    }

    /**
     * The name for this type. Defaults to the first string in the {@code Symbol} on the class.
     *
     * @return The name.
     */
    public @NonNull String getName() {
        Set<String> symbolValues = SymbolLookup.getSymbolValue(this);
        if (symbolValues.isEmpty()) {
            throw new IllegalArgumentException(clazz.getSimpleName() + " descriptor class " + this.getClass().getName()
                    + " does not have a @Symbol and does not override getName().");
        }
        return symbolValues.iterator().next();
    }

    /**
     * The full package and class name for the {@link WithScriptScript} class corresponding to this. Defaults to
     * the {@link WithScriptDescribable} class name with "Script" appended to the end.
     *
     * @return The class name, defaulting to the {@link WithScriptDescribable} {@link #clazz} class name with "Script" appended.
     */
    public @NonNull String getScriptClass() {
        return clazz.getName() + "Script";
    }

    @SuppressFBWarnings(value = "UI_INHERITANCE_UNSAFE_GETRESOURCE", justification = "We intentionally want to use the class loader for the subclass in this case")
    private @NonNull URL getScriptResource() {
        String scriptFile = '/' + getScriptClass().replace('.', '/') + ".groovy";
        return Objects.requireNonNull(getClass().getResource(scriptFile),
                () -> "Unable to find resource file: " + scriptFile);
    }

    /**
     * Creates an instance of the corresponding {@link WithScriptDescribable} from the given arguments.
     *
     * @param arguments A map of strings/objects to be passed to the constructor.
     * @return An instantiated {@link WithScriptDescribable}
     * @throws Exception if there are issues instantiating
     */
    public T newInstance(Map<String,Object> arguments) throws Exception {
        return new DescribableModel<>(clazz).instantiate(arguments);
    }

    /**
     * Creates an instance of the corresponding {@link WithScriptDescribable} with no arguments.
     *
     * @return An instantiated {@link WithScriptDescribable}
     * @throws Exception if there are issues instantiating
     */
    public T newInstance() throws Exception {
        return clazz.newInstance();
    }

    @Extension
    public static class WithScriptAllowlist extends GroovySourceFileAllowlist {
        private final Set<String> scriptUrls = new HashSet<>();

        private void registerScript(URL scriptUrl) {
            scriptUrls.add(scriptUrl.toString());
        }

        @Override
        public boolean isAllowed(String groovyResourceUrl) {
            return scriptUrls.contains(groovyResourceUrl);
        }
    }
}
