/*
 * Copyright © 2025 eXOReaction AS (rickard@exoreaction.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.xorcery.alchemy.script;

import javax.script.*;

public interface ScriptExecutor {

    static ScriptExecutor getScriptExecutor(ScriptEngine engine, String script)
            throws IllegalArgumentException
    {
        if (engine instanceof Compilable compilable)
        {
            try {
                CompiledScript compiledScript = compilable.compile(script);
                return compiledScript::eval;
            } catch (ScriptException e) {
                throw new IllegalArgumentException(e);
            }
        } else
        {
            return bindings -> engine.eval(script, bindings);
        }
    }

    void call(Bindings bindings)
            throws ScriptException;
}
