/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.impls;

import de.monticore.lang.embeddedmontiarc.tagging.middleware.MiddlewareSymbol;
import de.monticore.lang.tagging._symboltable.TagKind;

public class DummyMiddlewareSymbol extends MiddlewareSymbol {
    public static final DummyMiddlewareKind KIND = DummyMiddlewareKind.INSTANCE;

    public DummyMiddlewareSymbol() {
        super(KIND);
    }

    private static class DummyMiddlewareKind extends TagKind {
        public static final DummyMiddlewareKind INSTANCE = new DummyMiddlewareKind();

        protected DummyMiddlewareKind() {

        }
    }
}
