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
use Daemon::Daemonize qw/ daemonize write_pidfile /;
use FindBin;


#global const's
my $TESTFLAG = 0;
#global var's
my %Args;
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
sub main {
    if(defined $Args{pidfile}) {
        write_pidfile( $Args{pidfile} );
    }
    unlink $Args{tmpfile} or warn "Could not unlink $Args{tmpfile} $!";
    while(1){
        printf(STDERR "run: %s\n",$Args{cmd});
		if(defined $Args{stderr}) {
        	system(sprintf("%s 2> %s",$Args{cmd},$Args{stderr}));
		} else {
        	system(sprintf("%s",$Args{cmd}));
		}
        if( -f $Args{tmpfile}){
            printf(STDERR "true branch\n");
            my $json = loadJsonFromFile($Args{tmpfile});
            unlink $Args{tmpfile} or warn "Could not unlink $Args{tmpfile} $!";
        } else {
            printf(STDERR "false branch\n");
            last;
        }
    }
    printf(STDERR "THE END\n");
}

#main
GetOptions(
	"cmd=s" => \$Args{cmd},
	"tmpfile=s" => \$Args{tmpfile},
    "daemonize" => \$Args{daemonize},
    "stderr=s" => \$Args{stderr},
    "pidfile=s" => \$Args{pidfile},
);
for(@ARGV) {
    if(/\.json$/) {
        my $json = loadJsonFromFile($_);
        for my $key (keys %$json) {
            $Args{$key} = $json->{$key};
        }
    }
}
printf(STDERR "got: %s\n",Dumper(\%Args));

if( $Args{daemonize} ) {
    printf(STDERR "daemonizing: %s\n",$FindBin::Bin);
    daemonize(run=>\&main,
        stderr=>sprintf("%s",$Args{stderr}),
        close=>'std',
        chdir=>sprintf("%s/../../..",$FindBin::Bin),
    );
} else {
	open my $stderr, '>:utf8',$Args{stderr};
	*STDERR = $stderr;
    main();
}
