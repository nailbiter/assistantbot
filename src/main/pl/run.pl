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
use JSON;
use Data::Dumper;


#global const's
my $TESTFLAG = 0;
#procedures
sub myExec{
	(my $cmd) = @_;
	printf("exec: _%s\n",$cmd);
	if(not $TESTFLAG){
		system($cmd);
	}
}
sub loadJsonFromFile{
	my $fn = shift;
	printf(STDERR "opening file %s\n",$fn);
	my $document;
	my $fh;
	if(open($fh, $fn)){
		$document = do { local $/; <$fh> };
	} else {
		$document ="{}";
	}
	printf(STDERR "doc: %s\n",$document);
	close($fh);
	return from_json($document);
}

#main
my $cmd;
my $tmpfile;
#my $cmdfile;
GetOptions(
	"cmd=s" => \$cmd,
	"tmpfile=s" => \$tmpfile,
#	"cmdfile=s" => \$cmdfile,
);
printf("cmd: \"%s\"\n",$cmd);
printf("tmpfile: \"%s\"\n",$tmpfile);
#printf("cmdfile: \"%s\"\n",$cmdfile);

while(1){
	printf("run: %s\n",$cmd);
	system($cmd);
	if( -f $tmpfile){
		printf("true branch\n");
		my $json = loadJsonFromFile($tmpfile);
		printf("got: %s\n",Dumper($json));
#		if(exists $json->{command}){
#			my $cmdjson = loadJsonFromFile($cmdfile);
#			if(exists $cmdjson->{$json->{command}}){
#				myExec($cmdjson->{$json->{command}});
#			} else {
#				printf("cannot execute %s\n",$json->{command});
#			}
#		}
		unlink $tmpfile or warn "Could not unlink $tmpfile: $!";
	} else {
		printf("false branch\n");
		last;
	}
}
printf("THE END\n");
