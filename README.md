# wot_gateways
Web of Things gateways for KNX and EnOcean

## Description
This is the source code of gateways exposing RESTful services for accessing KNX and EnOcean field devices. Regarding KNX, the configuration of the gateway is performed by loading an ETS project archive. Clients can read values and actuate through the gateway. EnOcean is more limited in terms of functionalities and configuration. Clients can only read the state of devices, no actuation is currently possible. Before the EnOcean gateway can be operated, the available devices have to be added and configured through the Web GUI.

The gateways work with either HTTP or CoAP. For more information please read the Wiki pages.
