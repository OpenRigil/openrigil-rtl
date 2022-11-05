set_device GW2A-LV18PG256C8/I7
set_option -use_sspi_as_gpio 1 -bit_security 0
add_file /path/to/openrigil-uart-wrapper-reset.vg
add_file /path/to/openrigil-uart.cst
run pnr
