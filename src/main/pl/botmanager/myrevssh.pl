#!/usr/bin/env perl 
#===============================================================================
#
#         FILE: myrevssh.pl
#
#        USAGE: ./myrevssh.pl  
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
#      CREATED: 12/30/18 15:40:42
#     REVISION: ---
#===============================================================================

use strict;
use warnings;
use utf8;


#global const's
my $REMOTE = 'inp9822058@alumni.cs.nctu.edu.tw';
my $UNIT = 10000;
my $CERTKEY = '';
#procedures
sub myRand{
	(my $a,my $b) = @_;
	return $a+int(rand($b-$a));
}

#main
if( 0 ){
    my $id = myRand(10000,20000);
    my $cmd = sprintf("daemonize ssh -R %d:localhost:22 %s",$id,$REMOTE);
#`$cmd`;
    my $num1=myRand(1*$UNIT,2*$UNIT);
    my $num2=myRand(3*$UNIT,4*$UNIT);

#echo `date` : $num1 : $num2 | ssh $CERTKEY $LOGIN 'cat >> ports.txt'
    $cmd = sprintf("daemonize /usr/bin/autossh -f -N -M %d -o \"PubkeyAuthentication=yes\" -o \"PasswordAuthentication=no\" %s -R %d %s -p 22 -vvv",$num1,$CERTKEY,$num2,$REMOTE);
    system($cmd);
    printf("ssh localhost -p %d\nssh localhost -p %d\n",$num1,$num2);
} else {
    system('/home/nailbiter/myrevssh.sh')
}
