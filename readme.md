# RTL

## Introduction

This the mono-repo for OpenRigil RTL for FPGA prototype, it will include:
1. Rocket-based CPU
2. RISC-V Crypto Extension
3. MMM Accelerator
4. USB 1.1 FS device
5. SoC designs

## Build the emulator

First you should follow the `Quick Start` section in playground, or more specifically, only these two steps are necessary.
```
make init # init submodules, only a few of them are necessary though.
make patch
```

Then you should build the firmware in [openrigil-firmware](https://github.com/OpenRigil/openrigil-firmware)

Now you should change the `/path/to/openrigil.hex` in `sanitytests/rocketchip/resources/vsrc/usbbootrom.rom.v` to your built `openrigil.hex`

After setting up environments/tools like mill/dtc/clang (see Arch/Nix guide below), you can run the following command to build the emulator.
```
make test
```

Then you could find the emulator in `out/VerilatorTest/build/emulator`. You could emulate for a while to see if the firmware was loaded or not.

```
./out/VerilatorTest/build/emulator +verbose /dev/null
```

## FPGA

Now you can find the generated verilog in `out/VerilatorTest/` and you can import then in your EDA. Also Remember to import the `usbbootrom.rom.v` and corresponding `openrigil.hex`.

For Vivado, we have some reference design files in `vivado/`. Note that you should generate a 60MHz clock in block design and connect that to the clk pin for `truetop`. You can change it to other frequencies in `sanitytests/rocketchip/src/OpenRigil.scala` but it must be multiple of 12MHz and at least 48MHz is recommended (otherwise USB may not function well, we have not tested it yet.)

The constraint file is for for Arty A7-100T. You may also use it for Arty A7-35T. Note that USB traffic are carried through GPIO pins with pullup. You should prepare a dupont to USB Type-A board.

## Project Management
It will depend on https://github.com/sequencer/playground for keep updating with upstream dependency. When playground updates, this project should be totally rebased to that. After development, it will be rebase out from playground as a standalone project.  
`RocketCore` will be forked out to `RigilCore` in `rigil` directory.  
It will depend on `diplomacy` SoC framework and TileLink interconnection protocol before TileLink landing to its own repository.
Some other RTL(USB, DDR Controller) will be incubated inside this project as well, and finally being split to their own project to chipsalliance.  
Physical Design is also included but won't be open sourced.  

# Below are playground template
DONT MODIFY! UNLESS YOU WANT TO HANDLE CONFLICT EVERYDAY!

# playground

## Introduction
This is a template repository for those who want to develop RTL based on rocket-chip and even chipyard, being able to edit all sources from chisel environments without publish them to local ivy.
You can add your own submodule in `build.sc`.  
For more information please visit [Mill documentation](https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html)
after adding your own code, you can add your library to playground dependency, and re-index Intellij to add your own library.

## Quick Start

To use this repo as your Chisel development environment, simply follow the steps.

0. Clone this repo;

```bash
git clone git@github.com:sequencer/playground.git
```

0. Install dependencies and setup environments:
- Arch Linux `pacman -Syu --noconfirm make parallel wget cmake ninja mill dtc verilator git llvm clang lld protobuf antlr4 numactl`
- Nix `nix-shell`

0. [Optional] Remove unused dependences to accelerate bsp compile in `build.sc` `playground.moduleDeps`;

```bash
cd playground # entry your project directory
vim build.sc
```

```scala
// build.sc

// Original
object playground extends CommonModule {
  override def moduleDeps = super.moduleDeps ++ Seq(myrocketchip, inclusivecache, blocks, rocketdsputils, shells, firesim, boom, chipyard, chipyard.fpga, chipyard.utilities, mychiseltest)
  ...
}

// Remove unused dependences, e.g.,
object playground extends CommonModule {
  override def moduleDeps = super.moduleDeps ++ Seq(mychiseltest)
  ...
}
```


0. Init and update dependences;

```bash
cd playground # entry your project directory
make init     # init the submodules
make patch    # using the correct patches for some repos
```


0. Generate IDE bsp;

```bash
make bsp
```


0. Open your IDE and wait bsp compile;

```bash
idea . # open IDEA at current directory
```
06. Enjory your development with playground :)

## IDE support
For mill use
```bash
mill mill.bsp.BSP/install
```
then open by your favorite IDE, which supports [BSP](https://build-server-protocol.github.io/) 

## Pending PRs
Philosophy of this repository is **fast break and fast fix**.
This repository always tracks remote developing branches, it may need some patches to work, `make patch` will append below in sequence:
<!-- BEGIN-PATCH -->
cva6-wrapper https://github.com/ucb-bar/cva6-wrapper/pull/15  
chipyard https://github.com/ucb-bar/chipyard/pull/1160  
dsptools https://github.com/ucb-bar/dsptools/pull/240  
riscv-sodor https://github.com/ucb-bar/riscv-sodor/pull/72  
riscv-boom https://github.com/riscv-boom/riscv-boom/pull/600  
riscv-boom https://github.com/riscv-boom/riscv-boom/pull/601  
rocket-chip https://github.com/chipsalliance/rocket-chip/pull/2968  
rocket-chip-blocks https://github.com/chipsalliance/rocket-chip-blocks/pull/1  
rocket-chip-blocks https://github.com/chipsalliance/rocket-chip-blocks/pull/2  
rocket-chip-blocks https://github.com/chipsalliance/rocket-chip-blocks/pull/3  
rocket-chip-fpga-shells https://github.com/chipsalliance/rocket-chip-fpga-shells/pull/1  
rocket-chip-fpga-shells https://github.com/chipsalliance/rocket-chip-fpga-shells/pull/2  
rocket-chip-fpga-shells https://github.com/chipsalliance/rocket-chip-fpga-shells/pull/3  
rocket-chip-inclusive-cache https://github.com/chipsalliance/rocket-chip-inclusive-cache/pull/2  
rocket-dsp-utils https://github.com/ucb-bar/rocket-dsp-utils/pull/6  
<!-- END-PATCH -->

## Why not Chipyard

1. Building Chisel and FIRRTL from sources, get rid of any version issue. You can view Chisel/FIRRTL source codes from IDEA.
1. No more make+sbt: Scala dependencies are managed by mill -> bsp -> IDEA, minimal IDEA indexing time.
1. flatten git submodule in dependency, get rid of submodule recursive update.

So generally, this repo is the fast and cleanest way to start your Chisel project codebase.

## Always keep update-to-date
You can use this template and start your own job by appending commits on it. GitHub Action will automatically bump all dependencies, you can merge or rebase `sequencer/master` to your branch.

```bash
cd playground # entry your project directory
git rebase origin/master
```

## System Dependencies
Currently, only support **Arch Linux**, if you are using other distros please install nix.

* GNU Make
  - Arch Linux: make
* git
  - Arch Linux: git
* mill
  - Arch Linux: mill
* wget
  - Arch Linux: wget
* GNU Parallel
  - Arch Linux: parallel
* Device Tree Compiler
  - Arch Linux: dtc
* protobuf
  - Arch Linux: protobuf
* antlr4
  - Arch Linux: antlr4

## SanityTests
This package is the standalone tests to check is bumping correct or not, served as the unittest, this also can be a great example to illustrate usages.

**NOTICE: SanityTests also contains additional system dependencies:**
* clang: bootrom cross compiling and veriltor C++ -> binary compiling
  - Arch Linux: clang
* llvm: gnu toolchain replacement 
  - Arch Linux: llvm
* lld: LLVM based linker
  - Arch Linux: lld
* verilator -> Verilog -> C++ generation
  - Arch Linux: verilator numactl
* cmake -> verilator emulator build system
  - Arch Linux: cmake
* ninja -> verilator emulator build system
  - Arch Linux: ninja

### rocketchip
This package is a replacement to RocketChip Makefile based generator, it directly generate a simple RocketChip emulator with verilator and linked to spike. 
```
mill sanitytests.rocketchip
```

### vcu118
This package uses rocketchip and fpga-shells to elaborate FPGA bitstream generator and debug script with board [VCU118](https://www.xilinx.com/products/boards-and-kits/vcu118.html)
```
mill sanitytests.vcu118
```
If you wanna alter this to your own board, you can choose implmenting your own Shell to replace `VCU118Shell` in this test.
