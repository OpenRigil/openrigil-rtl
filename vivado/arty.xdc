## FPGA Configuration I/O Options
set_property CONFIG_VOLTAGE 3.3 [current_design]
set_property CFGBVS VCCO [current_design]
# FPGA bitstream options for MCS Generation
set_property BITSTREAM.CONFIG.SPI_BUSWIDTH 4 [current_design]

## Board Clock: 100 MHz
#create_clock -period 10.000 -name clk_100MHz [get_ports clk_100MHz]
set_property -dict {PACKAGE_PIN E3 IOSTANDARD LVCMOS33} [get_ports clk_100MHz]

## you can use this clock if you divided frequency for Montgomery accelerator
#create_generated_clock -name clk_20MHz -divide_by 3 -source [get_pins design_1_i/truetop_0/inst/openrigil/clk] [get_pins -hierarchical * -filter { name =~ "*/div_clock_div3/clk_out_reg/Q"}]

set_property IOSTANDARD LVCMOS33 [get_ports usb_dp]
set_property IOB FALSE [get_ports usb_dp]
set_property PACKAGE_PIN M18 [get_ports usb_dp] # In schematic M18 could be pulled up by A13
set_property SLEW SLOW [get_ports usb_dp]
set_property DRIVE 8 [get_ports usb_dp]

set_property IOSTANDARD LVCMOS33 [get_ports usb_dn]
set_property IOB FALSE [get_ports usb_dn]
set_property PACKAGE_PIN L18 [get_ports usb_dn]
set_property SLEW SLOW [get_ports usb_dn]
set_property DRIVE 8 [get_ports usb_dn]

set_property -dict {PACKAGE_PIN A13 IOSTANDARD LVCMOS33} [get_ports usb_pullup]

set_property -dict {PACKAGE_PIN A9  IOSTANDARD LVCMOS33} [get_ports uart_0_rxd]
set_property -dict {PACKAGE_PIN D10 IOSTANDARD LVCMOS33} [get_ports uart_0_txd]


set_property -dict { PACKAGE_PIN L16   IOSTANDARD LVCMOS33  IOB TRUE } [get_ports { qspi_sck }];
set_property -dict { PACKAGE_PIN L13   IOSTANDARD LVCMOS33  IOB TRUE } [get_ports { qspi_cs }]; #IO_L6P_T0_FCS_B_14 Sch=qspi_cs
set_property -dict { PACKAGE_PIN K17   IOSTANDARD LVCMOS33  IOB TRUE  PULLUP TRUE } [get_ports { qspi_dq_0 }]; #IO_L1P_T0_D00_MOSI_14 Sch=qspi_dq[0]
set_property -dict { PACKAGE_PIN K18   IOSTANDARD LVCMOS33  IOB TRUE  PULLUP TRUE } [get_ports { qspi_dq_1 }]; #IO_L1N_T0_D01_DIN_14 Sch=qspi_dq[1]
set_property -dict { PACKAGE_PIN L14   IOSTANDARD LVCMOS33  IOB TRUE  PULLUP TRUE } [get_ports { qspi_dq_2 }]; #IO_L2P_T0_D02_14 Sch=qspi_dq[2]
set_property -dict { PACKAGE_PIN M14   IOSTANDARD LVCMOS33  IOB TRUE  PULLUP TRUE } [get_ports { qspi_dq_3 }]; #IO_L2N_T0_D03_14 Sch=qspi_dq[3]
