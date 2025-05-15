/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.helpers;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAConnectorInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.tagging.middleware.MiddlewareSymbol;
import de.monticore.lang.embeddedmontiarc.tagging.middleware.ros.RosConnectionSymbol;
import de.monticore.lang.monticar.generator.rosmsg.GeneratorRosMsg;
import de.monticore.lang.monticar.generator.rosmsg.RosMsg;
import de.se_rwth.commons.logging.Log;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class RosHelper {
    private RosHelper() {
    }


    public static void fixRosConnectionSymbols(EMAComponentInstanceSymbol componentInstanceSymbol, boolean ros2mode) {
        Collection<EMAConnectorInstanceSymbol> x = componentInstanceSymbol.getConnectorInstances();
        x.toString();
        componentInstanceSymbol.getConnectorInstances().stream()
                .forEach(connectorSymbol -> {
                    if(connectorSymbol.getSourcePort()!=null || connectorSymbol.getTargetPort()!=null) {
                        if (connectorSymbol.getSourcePort().isRosPort() && connectorSymbol.getTargetPort().isRosPort()) {
                            if (Objects.equals(connectorSymbol.getSourcePort().getComponentInstance(), componentInstanceSymbol)) {
                                //In port of supercomp
                                inferRosConnectionIfPossible(connectorSymbol);
                                generateRosConnectionIfPossible(connectorSymbol, ros2mode);
                            } else if (Objects.equals(connectorSymbol.getTargetPort().getComponentInstance(), componentInstanceSymbol)) {
                                //out port of supercomp
                                inferRosConnectionIfPossible(connectorSymbol);
                                generateRosConnectionIfPossible(connectorSymbol, ros2mode);
                            } else {
                                //In between subcomps
                                inferRosConnectionIfPossible(connectorSymbol);
                                generateRosConnectionIfPossible(connectorSymbol, ros2mode);
                            }
                        }
                    }else {
                        Log.error("RosHelper: Component "+connectorSymbol.getFullName()+
                                " has no port satisfying "+connectorSymbol.getSource()+
                                " or "+ connectorSymbol.getTarget());
                    }
                });
    }

    //Cannot be moved to GeneratorRosCpp: target port name needed for topic name
    private static void generateRosConnectionIfPossible(EMAConnectorInstanceSymbol connectorSymbol, boolean ros2mode) {
        MiddlewareSymbol sourceTag = connectorSymbol.getSourcePort().getMiddlewareSymbol().orElse(null);
        MiddlewareSymbol targetTag = connectorSymbol.getTargetPort().getMiddlewareSymbol().orElse(null);
        if (sourceTag == null || targetTag == null || !sourceTag.isKindOf(RosConnectionSymbol.KIND) || !targetTag.isKindOf(RosConnectionSymbol.KIND)) {
            Log.debug("Both sourcePort and targetPort need to have a RosConnectionSymbol", "RosConnectionSymbol");
            return;
        }
        RosConnectionSymbol rosConnectionA = (RosConnectionSymbol) sourceTag;
        RosConnectionSymbol rosConnectionB = (RosConnectionSymbol) targetTag;
        RosConnectionSymbol emptyRosConnection = new RosConnectionSymbol();
        if (!rosConnectionA.equals(emptyRosConnection) || !rosConnectionB.equals(emptyRosConnection)) {
            Log.debug("Will not override rosConnections that are not empty!", "RosConnectionSymbol");
            return;
        }

        //target port name is unique: each in port can only have one connection!
        String topicName = NameHelper.getNameTargetLanguage(connectorSymbol.getTargetPort().getFullName());
        RosMsg rosTypeA = GeneratorRosMsg.getRosType("struct_msgs", connectorSymbol.getTargetPort().getTypeReference(), ros2mode);
        RosMsg rosTypeB = GeneratorRosMsg.getRosType("struct_msgs", connectorSymbol.getSourcePort().getTypeReference(), ros2mode);
        if (!rosTypeA.equals(rosTypeB)) {
            Log.error("topicType mismatch! "
                    + connectorSymbol.getSourcePort().getFullName() + " has " + rosTypeB + " and "
                    + connectorSymbol.getTargetPort().getFullName() + " has " + rosTypeA);
            return;
        }

        if (rosTypeA.getFields().size() == 1) {
            connectorSymbol.getSourcePort().setMiddlewareSymbol(new RosConnectionSymbol(topicName, rosTypeB.getName(), rosTypeB.getFields().get(0).getName()));
            connectorSymbol.getTargetPort().setMiddlewareSymbol(new RosConnectionSymbol(topicName, rosTypeA.getName(), rosTypeA.getFields().get(0).getName()));
        } else {
            connectorSymbol.getSourcePort().setMiddlewareSymbol(new RosConnectionSymbol(topicName, rosTypeB.getName()));
            connectorSymbol.getTargetPort().setMiddlewareSymbol(new RosConnectionSymbol(topicName, rosTypeA.getName()));
        }
    }

    private static void inferRosConnectionIfPossible(EMAConnectorInstanceSymbol connectorSymbol) {
        inferRosConnectionIfPossible(connectorSymbol.getSourcePort(), connectorSymbol.getTargetPort());
        inferRosConnectionIfPossible(connectorSymbol.getTargetPort(), connectorSymbol.getSourcePort());
    }

    private static void inferRosConnectionIfPossible(EMAPortInstanceSymbol sourcePort, EMAPortInstanceSymbol targetPort) {
        MiddlewareSymbol sourceTag = sourcePort.getMiddlewareSymbol().orElse(null);
        MiddlewareSymbol targetTag = targetPort.getMiddlewareSymbol().orElse(null);
        if (sourceTag == null || targetTag == null || !sourceTag.isKindOf(RosConnectionSymbol.KIND) || !targetTag.isKindOf(RosConnectionSymbol.KIND)) {
            Log.debug("Both sourcePort and targetPort need to have a RosConnectionSymbol", "RosConnectionSymbol");
            return;
        }

        RosConnectionSymbol sourceRosConnection = (RosConnectionSymbol) sourceTag;
        RosConnectionSymbol targetRosConnection = (RosConnectionSymbol) targetTag;

        if (sourceRosConnection.getTopicName().isPresent() && !targetRosConnection.getTopicName().isPresent())
            targetRosConnection.setTopicName(sourceRosConnection.getTopicName().get());

        if (sourceRosConnection.getTopicType().isPresent() && !targetRosConnection.getTopicType().isPresent())
            targetRosConnection.setTopicType(sourceRosConnection.getTopicType().get());

        if (sourceRosConnection.getMsgField().isPresent() && !targetRosConnection.getMsgField().isPresent())
            targetRosConnection.setMsgField(sourceRosConnection.getMsgField().get());
    }
}
