require 'socket'
ip_address = Socket.ip_address_list.find { |ai| ai.ipv6? && !ai.ipv6_loopback? }.ip_address
print ip_address