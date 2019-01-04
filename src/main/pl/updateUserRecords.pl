#!/usr/bin/env perl 
#===============================================================================
#
#         FILE: updateUserRecords.pl
#
#        USAGE: ./updateUserRecords.pl  
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
#      CREATED: 12/29/18 19:45:53
#     REVISION: ---
#===============================================================================

use strict;
use warnings;
use utf8;
use Getopt::Long;
use Data::Dumper;
use MongoDB;
use JSON;


#global var's
my $Testflag = 0;
#procedures
sub loadJsonFromFile{
	(my $fn) = @_;
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
sub my_update_one{
	(my $coll,my @args) = @_;
	printf("update_one(%s,%s)\n",Dumper($args[0],$args[1]));
	$coll->update_one($args[0],$args[1]) unless($Testflag);
}

#main
my %commandline;
GetOptions(
	"json=s" => \$commandline{json},
	"password=s" => \$commandline{password},
	"testflag=i" => \$Testflag,
);
my $client = MongoDB->connect(sprintf("mongodb://%s:%s\@ds149672.mlab.com:49672/logistics","nailbiter",$commandline{password}));
#my $coll = $client->ns('logistics.users');
my $coll = $client->get_database('logistics')->get_collection('_users');
my $json = loadJsonFromFile($commandline{json});
for(keys(%$json)){
	my $key = $_;
	my %val = %{$json->{$_}};
	printf("key: %s\nval: %s\n",$key,Dumper(\%val));
	for(keys %val){
		my_update_one($coll,{name=>$key},{'$set'=>{$_=>$val{$_}}});
	}
}
