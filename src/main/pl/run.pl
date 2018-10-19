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


#procedures
#main
my $cmd, my $tmpfile;
GetOptions(
	"cmd=s" => \$cmd,
	"tmpfile=s" => \$tmpfile,
);
printf("cmd: \"%s\"\n",$cmd);
printf("tmpfile: \"%s\"\n",$tmpfile);

while(1){
	printf("run: %s\n",$cmd);
	system($cmd);
	if( -f $tmpfile){
		printf("true branch\n");
		unlink $tmpfile or warn "Could not unlink $tmpfile: $!";
	} else {
		printf("false branch\n");
		last;
	}
}
printf("THE END\n");
