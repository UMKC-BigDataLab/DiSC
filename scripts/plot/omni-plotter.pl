#!/usr/bin/perl
use strict;
use warnings;
use Getopt::Long;
use File::Path qw(make_path);
use Data::Dumper qw(Dumper);
use Cwd 'abs_path';

my $usage = "Usage: $0 -i <input-dir> -o <output-dir> -k <k1,k2,k3,...> -r <r1,r2,..> -n <number-of-nodes>
Example: $0 -i ~/higgs -o output -k 1,2,3 -r 40,80,120 -n 16

Generate input files for plotting from proccessed logs. Then generate the following figures and tables:
  1. One node relative error over time plot.
  2. All nodes last relative error table.
  3. All nodes relative error over time plot.
  4. Family list size average for all nodes.
  5. Messags statistics.
  6. Convergence statistics.

The input directory must contain subdirectories named as below:
  datasetName.numberOfNodes.r[*].k[*]

For example:
  twtr.16.r80.k1
  twtr.16.r40.k2
  twtr.16.r120.k3

Options:
  -i, --in-dir       input directory that contains results from processed logs
  -o, --out-dir      output directory to store the prepared results
  -k, --k-vals       LSH k values to be plotted
  -r, --r-vals       r values to be plotted
  -n, --num-nodes    number of nodes that were used in the experiment (must be multiple of 4)
  -h, --help         print this message

";

GetOptions( 'i|in-dir=s'     => \my $indir,
            'o|out-dir=s'    => \my $outdir,
            'k|k-vals=s'     => \my $kvalues,
            'r|r-vals=s'     => \my $rvalues,
            'n|num-nodes=s'  => \my $nnodes,
            'h|help'         => \my $helpOpt
          )
          or die($usage);

if (not defined $indir or not defined $outdir or not defined $kvalues  or not defined $rvalues or not defined $nnodes or defined $helpOpt) {
  die ($usage);
}

my ($dirname, $thisscript) = split(/\/([^\/]+)$/,  abs_path($0));
my $x0 ="3:30"; # x-axis start time
my $xn ="17:00";# x-axis end time

print "SAVING TO OUTPUT DIR: $outdir\n";
make_path("$outdir/input-data/ave-relerrs");
make_path("$outdir/plots/one-node");
make_path("$outdir/plots/all-nodes");
make_path("$outdir/plots/fam-list-size");
make_path("$outdir/tables");

opendir (DIR, $indir) or die ("ERROR: CANNOT OPEN $indir\n");
print "READING INPUT DIR: $indir\n";
print "GENERATING INPUT DATA..";
my @files = ();
while (my $subdir = readdir(DIR)) {
  next if ($subdir =~ m/^\./);
  push @files, $subdir;
  my $return = system("$dirname/parse-relative-err.py $indir/$subdir/relative_error_statistics.txt  $outdir/input-data/$subdir");
}
closedir(DIR);
print "DONE.\n";

print "K: $kvalues\n";
print "R: $rvalues\n";
my @K = split /,/, $kvalues;
my @R = split /,/, $rvalues;
my $node = "3";

print "GENERATING INDIVIDUAL RELATIVE ERROR PLOTTING SCRIPT FOR NODE ($node-0)..";

my $plotStr = "
set timefmt '%H:%M'
set ylabel 'Avg. relative error (%)'
set xlabel 'Time (MM:SS)'

unset key
set grid
#set title 'Table 1'
set xtics rotate right
#set xtics 0,1800,96000

set xdata time
#set mxtics 2
set format x '%H:%M'

set yrange [0:100]
set xrange[\'$x0\':\'$xn\']
set ytics  0,10,100

#set style data line
set style line 1 lc rgb 'blue'   lt 1 lw 2.5 pt 5 ps 1.5   # --- blue
set style line 2 lc rgb 'red'    lt 1 lw 2.5 pt 9 ps 1.5   # --- red
set style line 3 lc rgb 'black'  lt 1 lw 2.5 pt 4 ps 1.5   # --- black
set style line 4 lc rgb '#228B22' lt 1 lw 2.5 pt 7 ps 1.5  # --- green

#set key left top
set key top
#set terminal postscript eps enhanced color font 'Times-Roman,15'
set terminal pdf enhanced font 'Times,15'

";

foreach my $k (@K) {
  $plotStr =  $plotStr . "\nset output \"$outdir/plots/one-node/K$k-Node$node.pdf\"\nplot ";
  my $lpt = 1;
  foreach my $r (@R) {
    # filter array based on k and r values
    my @filename = grep { /.*r$r.*$k/ } @files;
    $plotStr =  $plotStr . "  \"$outdir/input-data/@filename/$node.txt\" u 1:2 index 0  title 'r=$r'  with linespoints ls $lpt,\\\n";
    $lpt++;
  }
}
print "DONE.\n";
print "EXECUTING INDIVIDUAL RELATIVE ERROR PLOTTING SCRIPT FOR NODE ($node-0)..";

my $scriptName = "one-node-plot-relErr.gp";
open(FH, '>', $scriptName) or die $!;
print FH $plotStr;
close(FH);

system("gnuplot $scriptName");
system("rm $scriptName");

print "DONE.\n";



sub by_number {
    my ( $anum ) = $a =~ /(\d+)/;
    my ( $bnum ) = $b =~ /(\d+)/;
    ( $anum || 0 ) <=> ( $bnum || 0 );
}

print "GENERATING AVERAGE RELATIVE ERROR ACHIEVED ON ALL NODES..";
my $lastRelErrStr ="";
my $headerStr;
foreach my $r (@R) {
  foreach my $k (@K) {
    my @inputfile = grep { /.*r$r.*$k/ } @files;
    $lastRelErrStr = $lastRelErrStr . "$r\t$k\t";
    opendir(TDIR, "$outdir/input-data/@inputfile" ) || die "CAN'T OPEN $outdir/input-data/@inputfile: $!";
    my @nodesfile = grep { /txt$/ } readdir(TDIR);
    closedir TDIR;
    @nodesfile = sort by_number @nodesfile;

    my $localHeaderStr = "";
    foreach my $nodefile (@nodesfile){
      $lastRelErrStr = $lastRelErrStr . `grep '.' $outdir/input-data/@inputfile/$nodefile | tail -n 1 | cut -d '	' -f2 | tr -d '\n'` ."\t";
      $nodefile  =~ s/\.txt//;
      $localHeaderStr = $localHeaderStr . "\t$nodefile" ;
    }
    if (not defined $headerStr){
      $headerStr = "R\tK". $localHeaderStr . "\n";
    }
    $lastRelErrStr = $lastRelErrStr . "\n";
  }
}


open(FH, '>', "$outdir/tables/last-rel-err.txt") or die $!;
print FH $headerStr . $lastRelErrStr;
close(FH);

print "DONE.\n";

print "GENERATING RELATIVE ERROR PLOTTING SCRIPT FOR ALL NODES..";

$plotStr = "
set timefmt '%H:%M'
set ylabel 'Avg. relative error (%)' offset 2,0,0

set xlabel 'Time (MM:SS)'

unset key
set grid
#set title 'Table 1'
set xtics rotate right
#set xtics 0,1800,96000

set xdata time
#set mxtics 2
set format x '%H:%M'


set yrange [0:100]
set xrange[\'$x0\':\'$xn\']
set ytics  0,10,100

#set style data line
set style line 1 lc rgb 'blue'    lt 1 lw 1.5 pt 5 ps 0.50   # --- blue
set style line 2 lc rgb 'red'     lt 1 lw 1.5 pt 9 ps 0.50   # --- red
set style line 3 lc rgb 'black'   lt 1 lw 1.5 pt 4 ps 0.50   # --- black
set style line 4 lc rgb '#228B22' lt 1 lw 1.5 pt 7 ps 0.50  # --- green


#set key left top
set key top
#set size 0.75, 1
#set terminal postscript eps enhanced color font 'Times-Roman,15'
set terminal pdf enhanced font 'Times,5' size 1.4in, 1.1in
#set size ratio 0.75

";

foreach my $k (@K) {
  foreach my $r (@R) {
    my $i = 1;
    my @filename = grep { /.*r$r.*$k/ } @files;
    foreach my $j (1..$nnodes/4){
      $plotStr =  $plotStr . "\nset output \"$outdir/plots/all-nodes/K$k-R$r-Nodes$i"."to".($i+3).".pdf\"\nplot ";
      foreach my $k (1..4){
        # filter array based on k and r values
        $plotStr = $plotStr . " \"$outdir/input-data/@filename/$i.txt\" u 1:2 index 0  title columnheader(1)  with linespoints ls $k,\\\n";
        $i++;

      }
      $plotStr = $plotStr . "\n";
    }# end for i
  }# end for r
}# end for k
print "DONE.\n";
print "EXECUTING RELATIVE ERROR PLOTTING SCRIPT FOR ALL NODES..";

$scriptName = "all-nodes-plot-relErr.gp";
open(FH, '>', $scriptName) or die $!;
print FH $plotStr;
close(FH);

system("gnuplot $scriptName");
system("rm $scriptName");
print "DONE.\n";


print "PLOTTING FAMILY LIST REDUCTION..";
$plotStr = "
unset key
#set grid

#set xtics rotate right
#set xtics 0,1800,96000

#set mxtics 2

#set ytics  0,1,6
set yrange [0:10000]


set format y '%.0f'
set key samplen 1


set style line 1 lc rgb 'red'    lt 1 lw 2.5 pt 1 ps 0.5   # --- red
set style line 2 lc rgb 'blue'  lt 1 lw 2.5 pt 1 ps 0.5   # --- blue
set style line 3 lc rgb '#228B22' lt 1 lw 2.5 pt 1 ps 1.5  # --- green


#set key left top
set key left out horiz
set key width 10

#set terminal postscript eps enhanced color font 'Times-Roman,15'
set terminal pdf enhanced font 'Times,18' size 8in, 4in

#######################################

set grid y

set style data histogram
set style histogram cluster gap 3
set boxwidth 0.7
set style fill pattern

set auto x


set ylabel 'Avg. family size'
set size ratio -1
set xlabel 'Node

";

foreach my $r (@R) {
  my $filesStr = "";
  my $listSizesHeader = "k";
  foreach my $k (@K) {
    $listSizesHeader = $listSizesHeader . "\tk=$k";
    my @filenames = grep { /.*r$r.*$k/ } @files;
    $filesStr = $filesStr .  "$indir/@filenames/visible-families-stats.txt ";
  }

  my $listSizes = `/$dirname/get-ave-list-size.py $filesStr`;
  # write data to input file
  my $inputfilename = "$outdir/input-data/ave-relerrs/r$r.txt";
  open(FH, '>', $inputfilename) or die $!;
  print FH $listSizesHeader ."\n". $listSizes;
  close(FH);

  $plotStr = $plotStr . "set output  \"$outdir/plots/fam-list-size/r$r.pdf\"\nplot ";
  $plotStr = $plotStr . " \"$inputfilename\" using 2:xtic(1) ti col fs solid 7 ls 1,\\
        '' u 3 ti col  fs solid 10  ls 2, \\
        '' u 4 ti col  fs solid 12  ls 3\n\n";
}

my $scrtptname = "plot-ave-family-list.gp";
open(FH, '>', $scrtptname ) or die $!;
print FH $plotStr;
close(FH);

system("gnuplot $scrtptname");
system("rm $scrtptname");
print "DONE.\n";

print "GENERATING MESSAGE STATS..";

# init rows
my %row;
foreach my $k (@K) {
    $row{$k} = $k;
}

my $cols_str = "k\tn-msgs\tloss%\tav-msg-size";
my $tbl_header="";
my $i = 0;
foreach my $r (@R) {
  if ($i != 0) {
    $tbl_header = $tbl_header . "\t\t\t";
    $cols_str = $cols_str . "\tn-msgs\tloss%\tav-msg-size";
  }
  else { $i = 1; }

  $tbl_header = $tbl_header . "\tr=$r";
  foreach my $k (@K) {
    my @filenames = grep { /.*r$r.*$k/ } @files;
    # get cols
    my $num_msgs = `grep 'Number of packtes sent'  $indir/@filenames/output.txt | cut -d ':' -f2 | xargs | tr -d '\n'`;
    my $pk_loss =  `grep 'Packet Loss Rate'  $indir/@filenames/output.txt | cut -d ':' -f2 | xargs | tr -d '\n'`;
    my $msg_size = `grep 'Ave Size After Compression'  $indir/@filenames/output.txt | cut -d ':' -f2 | xargs | cut -d ' ' -f1 | tr -d '\n' `;

    $row{$k} = $row{$k} . "\t$num_msgs\t$pk_loss\t$msg_size";
  }
}

my $outtable = "$outdir/tables/msgs-sent.txt";
open(FH, '>', $outtable) or die $!;
print FH  $tbl_header . "\n";
print FH  $cols_str . "\n";

foreach my $k (@K) {
  print FH  $row{$k}  . "\n";
}
close(FH);

print "DONE.\n";

print "GENERATING TIME/CONVERGENCE STATS..";

$tbl_header = "    ";
my $tbl_subheader = "Time";

#assuming all r,k experiments used the same runtime, pick the first @ [0]
my $row = ` grep 'Duration:'  $indir/$files[0]/output.txt | cut -d ':' -f2  | xargs | sed 's/ //' | tr -d '\n' `;
my $tcount;
foreach my $r (@R) {
  $tbl_header = $tbl_header . "\tr=$r";
  my $col_header ="";
  foreach my $k (@K) {
    $tbl_subheader = $tbl_subheader . "\tFirst      \tLast       ";
    my @filenames = grep { /.*r$r.*$k/ } @files;
    $col_header = $col_header .  "\tFirst      \tLast       ";
    # get cols
    my $first_conv = `grep 'First Node  Node'  $indir/@filenames/output.txt | rev | cut -d ' ' -f1 | rev | sed 's/...\$//'| tr -d '\n'`;
    my $last_conv  = `grep 'Last  Node  Node'  $indir/@filenames/output.txt | rev | cut -d ' ' -f1 | rev | sed 's/...\$//' | tr -d '\n'`;
    $row = $row . "\t$first_conv\t$last_conv";
  }
  $row = $row . "\t";
  $tbl_subheader =   $tbl_subheader . "\t";
  my $space = `echo \"$col_header\" |  sed  -e "s/[A-Za-z]/ /g" | tr -d '\n'`;
  $tbl_header = $tbl_header . substr($space, (length "r=$r")*2) . "\t";
}

$outtable = "$outdir/tables/convergence-table.txt";
open(FH, '>', $outtable) or die $!;
print FH  $tbl_header . "\n" . $tbl_subheader. "\n" . $row."\n";
close(FH);

print "DONE.\n";


