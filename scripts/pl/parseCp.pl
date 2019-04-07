#!/usr/bin/env perl 
#===============================================================================
#
#         FILE: parseCp.pl
#
#        USAGE: ./parseCp.pl  
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
#      CREATED: 11/20/18 00:46:53
#     REVISION: ---
#===============================================================================

use strict;
use warnings;
use utf8;
use Getopt::Long;


#main
my $home = `echo \$HOME`;
chomp $home;
my $bogus;
GetOptions(
	"bogus=s" => \$bogus,
);
while(<>){
    chomp $_;
    if(/^Downloading:/){
        next;
    }
    if(/^\[[A-Z]+\]/){
        next;
    }
    s/$home/$bogus/g;
    printf("%s\n",$_);
}
#printf("home=\"%s\"",$home);
