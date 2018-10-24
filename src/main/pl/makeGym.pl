#!/usr/bin/env perl 
#===============================================================================
#
#         FILE: makeGym.pl
#
#        USAGE: ./makeGym.pl  
#
#  DESCRIPTION: fill logistics.gym database
#
#      OPTIONS: ---
# REQUIREMENTS: ---
#         BUGS: ---
#        NOTES: ---
#       AUTHOR: YOUR NAME (), 
# ORGANIZATION: 
#      VERSION: 1.0
#      CREATED: 10/24/18 12:04:24
#     REVISION: ---
#===============================================================================

use strict;
use warnings;
use utf8;
use Getopt::Long;
use Data::Dumper;
use MongoDB;
use JSON;


#procedures
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
my $client = MongoDB->connect();
my $mongoPassword = $client->ns("admin.passwords")->find_one({key=>"MONGOMLAB"})->{value};
my $gymProgram;
GetOptions(
	"program=s" => \$gymProgram,
);
$gymProgram = loadJsonFromFile($gymProgram);
printf(STDERR "got: %s\n",Dumper($gymProgram));
MongoDB->connect(sprintf("mongodb://%s:%s\@ds149672.mlab.com:49672/logistics","nailbiter",$mongoPassword))->ns("logistics.gymProgram")->insert_many($gymProgram);
