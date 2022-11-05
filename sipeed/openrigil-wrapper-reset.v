module OpenRigilWrapperReset(
  input   clock,
  input   nreset,
  output  uart_0_txd
);
  wire  ldut_clock;
  wire  ldut_reset;
  wire  ldut_uart_0_txd;
  wire  ldut_uart_0_rxd;
  OpenRigilSystem ldut (
    .clock(ldut_clock),
    .reset(ldut_reset),
    .uart_0_txd(ldut_uart_0_txd),
    .uart_0_rxd(ldut_uart_0_rxd)
  );
  assign ldut_clock = clock;
  assign ldut_reset = !nreset;
  assign ldut_uart_0_rxd = 0;
  assign uart_0_txd = ldut_uart_0_txd;
endmodule
