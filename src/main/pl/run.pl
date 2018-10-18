#!/usr/bin/env perl 
#===============================================================================
#
#         FILE: run.pl
#
#        USAGE: ./run.pl  
#
#  DESCRIPTION: 
#
#      OPTIONS: ---
# REQUIREMENTS: ---
#         BUGS: ---
#        NOTES: ---
#       AUTHOR: YOUR NAME (), 
# ORGANIZATION: 
#      VERSION: 1.0
#      CREATED: 10/18/18 22:10:22
#     REVISION: ---
#===============================================================================

use strict;
use warnings;
use utf8;
use Getopt::Long;


#main
my $cmd, my $tmpfile;
GetOptions(
	"cmd=s" => \$cmd,
	"tmpfile=s" => \$tmpfile,
);
printf("cmd: \"%s\"\n",$cmd);
printf("tmpfile: \"%s\"\n",$tmpfile);
system($cmd);
printf("THE END\n");
