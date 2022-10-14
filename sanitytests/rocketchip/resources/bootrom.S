#define MASKROM_BASE 0x100000

.section .text.start, "ax", @progbits
.globl _start
_start:
  csrwi 0x7c1, 0 // disable chicken bits
  //li s0, MASKROM_BASE
  //jr s0
  // enable uart
  li a0, 0x6000
  li a1, 0x1
  sw a1, 8(a0)
  // uart loop
1:
  li a2, 0x1000
  li a3, 1
2:
  sub a2, a2, a3
  bnez a2, 2b
  li a1, 65
  sw a1, 0(a0) // output A
  j 1b
