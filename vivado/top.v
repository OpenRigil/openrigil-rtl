`timescale 1ns / 1ps

module truetop(
    input clk,
    input locked,
    inout usb_dp,
    inout usb_dn,
    output usb_pullup,
    output uart_0_txd,
    input  uart_0_rxd,
    output qspi_sck,
    inout  qspi_dq_0,
    inout  qspi_dq_1,
    inout  qspi_dq_2,
    inout  qspi_dq_3,
    output qspi_cs
    );

    wire rocket_reset;
    assign rocket_reset = !locked;
        
    wire [1:0] rocket_io_in;
    wire [1:0] rocket_io_out;
    
    wire qspi_0_dq_0_i;
    wire qspi_0_dq_0_o;
    wire qspi_0_dq_0_ie;
    wire qspi_0_dq_0_oe;
    wire qspi_0_dq_1_i;
    wire qspi_0_dq_1_o;
    wire qspi_0_dq_1_ie;
    wire qspi_0_dq_1_oe;
    wire qspi_0_dq_2_i;
    wire qspi_0_dq_2_o;
    wire qspi_0_dq_2_ie;
    wire qspi_0_dq_2_oe;
    wire qspi_0_dq_3_i;
    wire qspi_0_dq_3_o;
    wire qspi_0_dq_3_ie;
    wire qspi_0_dq_3_oe;
    
    OpenRigilSystem openrigil (
    .clock(clk),
    .reset(rocket_reset),
    .usb_rx(rocket_io_in),
    .usb_tx(rocket_io_out),
    .usb_pullup(usb_pullup),
    .uart_0_txd(uart_0_txd),
    .uart_0_rxd(uart_0_rxd),
    .qspi_0_sck(qspi_sck),
    .qspi_0_dq_0_i(qspi_0_dq_0_i),
    .qspi_0_dq_0_o(qspi_0_dq_0_o),
    .qspi_0_dq_0_ie(qspi_0_dq_0_ie),
    .qspi_0_dq_0_oe(qspi_0_dq_0_oe),
    .qspi_0_dq_1_i(qspi_0_dq_1_i),
    .qspi_0_dq_1_o(qspi_0_dq_1_o),
    .qspi_0_dq_1_ie(qspi_0_dq_1_ie),
    .qspi_0_dq_1_oe(qspi_0_dq_1_oe),
    .qspi_0_dq_2_i(qspi_0_dq_2_i),
    .qspi_0_dq_2_o(qspi_0_dq_2_o),
    .qspi_0_dq_2_ie(qspi_0_dq_2_ie),
    .qspi_0_dq_2_oe(qspi_0_dq_2_oe),
    .qspi_0_dq_3_i(qspi_0_dq_3_i),
    .qspi_0_dq_3_o(qspi_0_dq_3_o),
    .qspi_0_dq_3_ie(qspi_0_dq_3_ie),
    .qspi_0_dq_3_oe(qspi_0_dq_3_oe),
    .qspi_0_cs_0(qspi_cs)
    );
            
    wire usb_tx_en;
    assign usb_tx_en = rocket_io_out != 2'b11;
    
    wire usb_dp_in;
    wire usb_dn_in;
    
    assign rocket_io_in = (!usb_tx_en)? {usb_dp_in,usb_dn_in}: 2'b10;
    
    IOBUF iobuf_dp (.IO(usb_dp), .I(rocket_io_out[1]), .O(usb_dp_in), .T(!usb_tx_en));
    IOBUF iobuf_dn (.IO(usb_dn), .I(rocket_io_out[0]), .O(usb_dn_in), .T(!usb_tx_en));
    
    IOBUF iobuf_dq_0 (.IO(qspi_dq_0), .I(qspi_0_dq_0_o), .O(qspi_0_dq_0_i), .T(!qspi_0_dq_0_oe));
    IOBUF iobuf_dq_1 (.IO(qspi_dq_1), .I(qspi_0_dq_1_o), .O(qspi_0_dq_1_i), .T(!qspi_0_dq_1_oe));
    IOBUF iobuf_dq_2 (.IO(qspi_dq_2), .I(qspi_0_dq_2_o), .O(qspi_0_dq_2_i), .T(!qspi_0_dq_2_oe));
    IOBUF iobuf_dq_3 (.IO(qspi_dq_3), .I(qspi_0_dq_3_o), .O(qspi_0_dq_3_i), .T(!qspi_0_dq_3_oe));
endmodule
