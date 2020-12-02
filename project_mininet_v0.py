#!/usr/bin/python

# Python script for CS9657 Assignment 5:
# Build a mininet topology with 8 hosts, 14 switches, and a remote floodlight controller.
# Links have bandwidth 15Mbps, latency 1ms, packet loss 1%

from mininet.net import Mininet
from mininet.node import Controller, RemoteController, OVSController
from mininet.node import CPULimitedHost, Host, Node
from mininet.node import OVSKernelSwitch, UserSwitch
from mininet.node import IVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.link import TCLink, Intf
from subprocess import call
import os

# network for project simulation:
# 3 hosts, 5 switches, 1 controller.
#
# h1 -- s1
#       |
# h2 -- s2
#       |
#       s3
#       |
#       s4
#       |
# h3 -- s5
#
#----------------
# h1: represents DNS local resover
# h2: represents the attacker
# h3: represents the remote DNS server.
#
# The reeson why we use 5 swithes, is to simulate the senario, where DNS server is remotely located.
# Meanwhile, the attacker is close to local DNS resovler, and eversdropping the upcoming (and incoming) traffic.
def myNetwork():

    net = Mininet( topo=None,
                   build=False,
                   link=TCLink,
                   ipBase='10.0.0.0/8')

    info( '*** Adding controller\n' )
    # Add a remote controller which is floodlight
    # The floodlight should run first on another terminal.
    # Then We can run this script.
    #
    # The floodlight controller should have ip address 127.0.0.1
    # and it listens upcoming connections from port 6653.
    c0=net.addController(name='c0',
                      controller=RemoteController,
                      ip='127.0.0.1',
                      port=6653)

    info( '*** Add switches\n')
    s1 = net.addSwitch('s1', cls=OVSKernelSwitch)
    s2 = net.addSwitch('s2', cls=OVSKernelSwitch)
    s3 = net.addSwitch('s3', cls=OVSKernelSwitch)
    s4 = net.addSwitch('s4', cls=OVSKernelSwitch)
    s5 = net.addSwitch('s5', cls=OVSKernelSwitch)

    info( '*** Add hosts\n')
    h1 = net.addHost('h1', cls=Host, ip='10.0.0.1', defaultRoute=None)
    h2 = net.addHost('h2', cls=Host, ip='10.0.0.2', defaultRoute=None)
    h3 = net.addHost('h3', cls=Host, ip='10.0.0.3', defaultRoute=None)

    info( '*** Add links\n')
    # links between host and switch will have 1ms delay, and no packet loss
    net.addLink(h1, s1, bw=15, delay='1ms')
    net.addLink(h2, s2, bw=15, delay='1ms')
    net.addLink(h3, s5, bw=15, delay='1ms')
    # links between swithces will have 10ms delay, 1% packet loss
    net.addLink(s1, s2, bw=15, delay='10ms', loss=1)
    net.addLink(s2, s3, bw=15, delay='10ms', loss=1)
    net.addLink(s3, s4, bw=15, delay='10ms', loss=1)
    net.addLink(s4, s5, bw=15, delay='10ms', loss=1)
    
    info( '*** Starting network\n')
    net.build()
    info( '*** Starting controllers\n')
    for controller in net.controllers:
        controller.start()
    info( '*** Starting switches\n')
    net.get('s1').start([c0])
    net.get('s2').start([c0])
    net.get('s3').start([c0])
    net.get('s4').start([c0])
    net.get('s5').start([c0])

    # Start running command line interface
    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    myNetwork()

