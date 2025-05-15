/* (c) https://github.com/MontiCore/monticore */
package tests.someip;
conforms to de.monticore.lang.embeddedmontiarc.tagging.middleware.someip.SomeIPToEmamTagSchema;

tags Test {
tag testComp.in1 with SomeIPConnection = {serviceID = 11, instanceID = 12, eventgroupID = 13};
tag testComp.in2 with SomeIPConnection = {serviceID = 21, instanceID = 22, eventgroupID = 23};
tag testComp.in3 with SomeIPConnection = {serviceID = 31, instanceID = 32, eventgroupID = 33};
tag testComp.in4 with SomeIPConnection = {serviceID = 41, instanceID = 42, eventgroupID = 43};
tag testComp.out1 with SomeIPConnection = {serviceID = 111, instanceID = 112, eventgroupID = 113};
tag testComp.out2 with SomeIPConnection = {serviceID = 121, instanceID = 122, eventgroupID = 123};
tag testComp.out3 with SomeIPConnection = {serviceID = 131, instanceID = 132, eventgroupID = 133};
tag testComp.out4 with SomeIPConnection = {serviceID = 141, instanceID = 142, eventgroupID = 143};
}
