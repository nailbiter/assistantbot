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
use Text::Table;


#global const's
my $TESTFLAG = 0;
my $DEFMETHOD = 'help';
my %METHODS = (
	help => {
		description=>"show this message",
		function=>\&help,
	},
	jar => {
		description => 'make jar',
		function => sub {
			myExec('make jar');
		}
	},
	pull => {
		description => 'make pull',
		function => sub {
			myExec('make pull');
		}
	},
);
#global var's
#procedures
sub help{
	my $tb = Text::Table->new(
		"method",\' | ',"description",
	);
	$tb->add('-----','-------');
	for(keys(%METHODS)){
		$tb->add((($_ eq $DEFMETHOD)?'_':'').$_,$METHODS{$_}->{description});
	}
	print $tb;
}
sub myExec{
	(my $cmd) = @_;
	printf(STDERR "exec: _%s\n",$cmd);
	unless($TESTFLAG){
		system($cmd);
	}
}

#main
for(@ARGV){
	my $method = $_;
	chomp $method;
	my $func;
	if(exists $METHODS{$method}){
		$func = $METHODS{$method}->{function};
	} else {
		$func = $METHODS{$DEFMETHOD}->{function};
	}
	$func->();
}
