/*
 *                       PacketEye (Packet Sniffer) application
 *                      Developed as mini project in RGUKT Nuzvid
 *            Developed by Ayyappa Swamy, Rama Krishna, Vara Lakshmi and Mani
 *                               Completed in 07/04/2017
 */
 
 
 // This file contains code for capturing the packets, seggregating the packets and seggregating the data according to packets

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import jpcap.PacketReceiver;
import jpcap.packet.ARPPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;


class PacketPrinter implements PacketReceiver{
	
	private Date presTime;						//Time
	private String protocol,SrcMAC,DstMAC,time; //Protocol, source MAC Destination MAC and Time
	private int DstPort,SrcPort,length;         //Destination port, Source Port and length of the packet
	private String data;                        //Data in the packet
	private InetAddress DstIP,SrcIP;            //Source IP address and Destination IP address
	private String output;                      //Combined output data

	@Override
	public void receivePacket(Packet pckt) {
		presTime = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
		time = sdf.format(presTime);
		
		
		if(pckt instanceof ICMPPacket){
			GUI2.count++;
			ICMPPacket ICMPpckt = (ICMPPacket)pckt;
			protocol = "ICMP";
			SrcIP = ICMPpckt.src_ip;
			DstIP = ICMPpckt.dst_ip;
			SrcMAC = ICMPpckt.datalink.toString().substring(36, 54);
			DstMAC = ICMPpckt.datalink.toString().substring(55, 73);
			length = ICMPpckt.length;
			data = new String(ICMPpckt.data);
			output = String.format("\n%-6s\t%-14s\t%-10s%-16s\t%-16s\t%-20s\t%-20s\t%-8s\t%-8s\t%-6s\n%s",GUI2.count,time,protocol,SrcIP,DstIP,SrcMAC,DstMAC,SrcPort,DstPort,length,data);
			GUI2.displayResult.append(output+"\n\n");
		}
		else if(pckt instanceof TCPPacket){
			GUI2.count++;
			TCPPacket TCPpckt = (TCPPacket)pckt;
			protocol = "TCP";
			SrcIP = TCPpckt.src_ip;
			DstIP = TCPpckt.dst_ip;
			SrcMAC = TCPpckt.datalink.toString().substring(36, 54);
			DstMAC = TCPpckt.datalink.toString().substring(55, 73);
			SrcPort = TCPpckt.src_port;
			DstPort = TCPpckt.dst_port;
			length = TCPpckt.length;
			try{
				data = new String(TCPpckt.data,"ISO-8859-1");
			}catch (Exception e) {
				e.printStackTrace();
			}
			output = String.format("\n%-6s\t%-14s\t%-10s%-16s\t%-16s\t%-20s\t%-20s\t%-8s\t%-8s\t%-6s\n%s",GUI2.count,time,protocol,SrcIP,DstIP,SrcMAC,DstMAC,SrcPort,DstPort,length,data);
			GUI2.displayResult.append(output+"\n\n");
		}
		else if(pckt instanceof UDPPacket){
			GUI2.count++;
			UDPPacket UDPpckt = (UDPPacket) pckt ;
			protocol = "UDP";
			SrcIP = UDPpckt.src_ip;
			DstIP = UDPpckt.dst_ip;
			SrcMAC = UDPpckt.datalink.toString().substring(36, 54);
			DstMAC = UDPpckt.datalink.toString().substring(55, 73);
			SrcPort = UDPpckt.src_port;
			DstPort = UDPpckt.dst_port;
			length = UDPpckt.length;
			try{
				data = new String(UDPpckt.data,"ISO-8859-1");
			}catch (Exception e) {
				e.printStackTrace();
			}
			output = String.format("\n%-6s\t%-14s\t%-10s%-16s\t%-16s\t%-20s\t%-20s\t%-8s\t%-8s\t%-6s\n%s",GUI2.count,time,protocol,SrcIP,DstIP,SrcMAC,DstMAC,SrcPort,DstPort,length,data);
			GUI2.displayResult.append(output+"\n\n");
		}
		else if(pckt instanceof ARPPacket){
			GUI2.count++;
			ARPPacket ARPpckt = (ARPPacket) pckt ;
			if(ARPpckt.operation==1)
				protocol = "ARP(REQ)";
			else if(ARPpckt.operation==2)
				protocol = "ARP(REP)";
			SrcIP = (InetAddress) ARPpckt.getSenderProtocolAddress();
			DstIP = (InetAddress) ARPpckt.getTargetProtocolAddress();
			SrcMAC = (String) ARPpckt.getSenderHardwareAddress();
			DstMAC = (String) ARPpckt.getTargetHardwareAddress();
			SrcPort = 0;
			DstPort = 0;
			length = ARPpckt.len;
			data = new String(ARPpckt.data);
			output = String.format("\n%-6s\t%-14s\t%-10s%-16s\t%-16s\t%-20s\t%-20s\t%-8s\t%-8s\t%-6s\n%s",GUI2.count,time,protocol,SrcIP,DstIP,SrcMAC,DstMAC,SrcPort,DstPort,length,data);
			GUI2.displayResult.append(output+"\n\n");
		}		
	}
	
}
