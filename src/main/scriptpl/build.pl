#!/usr/bin/env perl 
#===============================================================================
#
#         FILE: build.pl
#
#        USAGE: ./build.pl  
#
#  DESCRIPTION: build
#
#      OPTIONS: ---
# REQUIREMENTS: ---
#         BUGS: ---
#        NOTES: ---
#       AUTHOR: YOUR NAME (), 
# ORGANIZATION: 
#      VERSION: 1.0
#      CREATED: 11/29/18 10:24:28
#     REVISION: ---
#===============================================================================

use strict;
use warnings;
use utf8;


#global const's
my $TESTFLAG = 0;
#global var's
#procedures
sub myExec{
	(my $cmd) = @_;
	printf(STDERR "exec: _%s\n",$cmd);
	unless($TESTFLAG){
		system($cmd);
	}
}
#main
(my $method) = @ARGV;
chomp $method;
if($method eq 'pulljar'){
	myExec('make pull jar');
} else {
	printf("unknown method %s\n",$method);
}
