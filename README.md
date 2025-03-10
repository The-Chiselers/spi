# Chisel Project Template

## Setup

### Git 

```bash
git clone [url].git
git submodule update --init --recursive
touch .git-blame-ignore-revs
git config blame.ignoreRevsFile .git-blame-ignore-revs
``` 

## Development

### IDE

Recommended IDE is VSCode

#### Plugins

Recommended Plugins are:

- Prettier
- Scala Syntax (Official)
- Scala (Metals)
- Nix

These can also be found in the recommended plugins for this project:

### Dependancies

#### The Easy Way (Nix)

If you are using the nix package manager, the simplest way to get setup is by running sh dev_shell.sh

Dependancies can be found in flake.nix, can use nix to install dependancies with `nix develop -c $SHELL`, or this command if flakes are not enabled: `nix develop --extra-experimental-features nix-command --extra-experimental-features flakes -c $SHELL`

#### Manually
- sbt
- scala-cli
- scalafmt
- chisel 5.3.0
- firtool 1.44.0
- sbt-scoverage
- verilator
- ninja
- cmake
- opensta
- yosys
- gtkwave
- iverilog
- latex

### Running

Run `sh run.sh`, this is a simple enough example to test if your install is working. You could also chose to run `sbt run`. If this command works then you have a working install and can continue your own developement

#### Output

Should look like the following:

```verilog
// Generated by CIRCT firtool-1.62.0
module FirFilter(
  input        clock,
               reset,
  input  [7:0] io_in,
  output [7:0] io_out
);

  reg [7:0] zs_0;
  reg [7:0] zs_1;
  reg [7:0] zs_2;
  always @(posedge clock) begin
    zs_0 <= io_in;
    zs_1 <= zs_0;
    zs_2 <= zs_1;
  end // always @(posedge)
  assign io_out = zs_0 + zs_1 + zs_2 * 8'h3;
endmodule
```

### Testbench
To run test with waveform: "sbt "testOnly <\TestName\> -- -DwriteVcd=1"

To open waveform: "gtkwave test_run_dir/->/*.vcd"
