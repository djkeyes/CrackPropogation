
c***************************************************************
c	Version 3.0
C	
C	SINCE  2010.10.15
c	Update 2014.03.15
c
c	FATIGUE LIFE ANALYSIS PROGRAM 
c
c     WRITED BY  Youngchul Park  
c	                           ypark@asdl.gatech.edu
c***************************************************************

	PROGRAM FLA_KC_100
	INTEGER P
	PARAMETER (P=10000000)
	REAL  SF, CF,TDAM, ans, MATno
	double precision VMAX(P),VMIN(P), SMAX(P), SMIN(P),R(P),SEQ(P)
	double precision SEQAU(P), NF(P), DAM(P), SDAM(P), CDAM(P), CYC(P)
	double precision X(P), Y(P), Z(P)
	INTEGER ELM(P), Stress(P), MatID(P)
	INTEGER  MatID_sn(P)
	REAL  NOM, NOP, NOS
	REAL  A(P), B(P), C(P), L(P)
C	dimension norm_stress(p,5),stress_spectrum(p,5)
      
	CHARACTER*3 lpd,flpd(p),LOAD,ans2

C	INTEGER LOAD
***************************************************************
C	### VARIABLE INDEX ###
C	
C	SF= SACTTER FACTOR
C	NOB= NUMBER OF BLOCK
C	
C	CF= CONVERSION FACTOR
C
C	### FILE INDEX ###
C	
C	
C
c	### SUBROUTINE INDEX ###
C
C
C
C
C***************************************************************

	WRITE(*,*)'****************************************************'
	WRITE(*,*)''
	WRITE(*,*)'       C  I  A  - Crack Initiation Analysis          '
	WRITE(*,*)''
	WRITE(*,*)'                    Version 2.0                       '
	WRITE(*,*)' '
	WRITE(*,*)'****************************************************'	


	WRITE(*,*) " Reading SN curve fitting data ..... " 


	SF=8
	NOB=10
	CF=0.001 
	MATno=4
	
      OPEN(UNIT=1,FILE='Mat_sn.dat')

	DO 10, I=1,MATno
	READ(1,*)  MatID_sn(I), A(I), B(I), C(I)
C	WRITE(*,*) MatID_sn(I), A(I), B(I), C(I) 
10	continue	
	CLOSE(1)


C	WRITE(*,1500) " A = ",  A
C	WRITE(*,1500) " B = ",  B
C	WRITE(*,1500) " C = ",  C
C	WRITE(*,*) " "
c	WRITE(*,*) " Reading analysis factors ..... " 
c	WRITE(*,1500) " Scatter factor = ",  SF
c	WRITE(*,1500) " Number of Blocks = ",  NOB
c	WRITE(*,1500) " Conversion factor(Mpa-Ksi) = ",  CF	 	
c	WRITE(*,*) " "	

1500	FORMAT(2X, G30.0 , 2X, G10.4 )


	NOP=797330

	WRITE(*,*) " "
	WRITE(*,*) "NUMBER OF LOAD POINTS =", NOP 	
	WRITE(*,*) " "

	open(UNIT=2,FILE='loadspectrum.dat',STATUS='OLD')

	DO 30, I=1,NOP

	read(2,*) L(I)
C	WRITE(*,*) L(I) 
30	CONTINUE
	close(2)

	WRITE(*,*) " " 


	WRITE(*,*) " Read Finite Element Analysis ......."
	WRITE(*,*) " " 

	OPEN(unit=3,file='stress.dat')
	
	NOS=7710

	DO 20,J=1,NOS
	read(3,*) ELM(J), Stress(J), MatID(J)
20	CONTINUE	

	close(3)


c ********** Start to loop ******************


	OPEN(UNIT=7,FILE='RESULT.DAT')



	DO 500, K=1,NOS

	WRITE(*,*) " Start Analysis for ELEMENT : ", ELM(K)
	WRITE(*,*) " Calculating stress spectrum  ......."

	OPEN(unit=4,file='stress history.txt',STATUS='REPLACE')

	DO 31,I=1,NOP

	WRITE(4,*) L(I)*Stress(K)

31	continue


	Close(4)

	open(UNIT=4,FILE='stress history.txt',STATUS='OLD',POSITION='APPEND')
      write(4,*) "end"
	close(4)
	

C
C	****************************************
C			RAINFLOW COUNTING
C
C	****************************************

	WRITE(*,*) " "
	WRITE(*,*) " PREPARE FOR THE RAINFLOW COUNTING ......."
	WRITE(*,*) " "

	Call SYSTEMQQ("RAINFLOW.exe")


	OPEN(unit=5,file='counted stress history.txt')
	
	READ(5,*) RM

	DO 40,I=1,P

	read(5,*) X(I), Y(I), Z(I)

	IF (X(I).EQ.0) THEN

	NOM=I-1

	GOTO 50
	END IF
40	CONTINUE	

50	close(5)


	OPEN(unit=5,file='counted stress history.txt')

	
	DO 60,I=1,NOM
	READ(5,*) VMAX(I), VMIN(I), CYC(I) 
60	CONTINUE

	CLOSE(5, status='delete')
	CLOSE(5)


	DO 70,I=1,NOM
	IF(VMAX(I).LT.0. AND. VMIN(I).LT.0) THEN 
	SMAX(I)=ABS(VMIN(I))
	ELSE 
	SMAX(I)=VMAX(I)
	END IF
70	CONTINUE

		

	DO 80,I=1,NOM
	IF(VMAX(I).LT.0. AND. VMIN(I).LT.0) THEN 
	SMIN(I)=ABS(VMAX(I))
	ELSE
	SMIN(I)=VMIN(I)
	END IF
80	CONTINUE


	DO 90,I=1,NOM
	R(I)= SMIN(I)/SMAX(I)
90	CONTINUE

C ************  CALCULATION S-N WITH MINER PARMGRAM RULE*******************

	DO 100,I=1,NOM
	SEQ(I)=SMAX(I)*(1-R(I))**A(MatID(K))
	
100	CONTINUE


	DO 110,I=1,NOM
	SEQAU(I)=SEQ(I)*CF
		
110	CONTINUE


	DO 120,I=1,NOM
	NF(I)=10**(B(MatID(K))-C(MatID(K))*LOG10(SEQAU(I)))
		
120	CONTINUE

	DO 130,I=1,NOM
	DAM(I)=CYC(I)/NF(I)
		
130	CONTINUE

C********* TO reduce time *********
c	OPEN(UNIT=6,FILE='RESULT_DATA.DAT')
c	DO 140,I=1,NOM
c	WRITE(6,1000) SMAX(I), SMIN(I),R(I),SEQ(I),SEQAU(I),NF(I),DAM(I)
c
c140	CONTINUE
c
c1000	FORMAT(3X, F10.3, 3X, F10.3, 3X, F10.3, 3X, F10.3, 3X,
c    $F10.3, 3X, F10.3, 3X, F10.7)

	CLOSE(6)
C********* TO reduce time *********

      DO 331, I=1,NOM

	CDAM(I)=0.0
		      
	DO 332,J=1,NOM
	CDAM(I)=CDAM(I)+DAM(J)
  332	CONTINUE

      SDAM=CDAM(I)

  331	CONTINUE



	TDAM=SDAM(NOM)*SF*NOB
	CIC=NOP/TDAM


	WRITE(*,*) "  "
	WRITE(*,*) " Analysis has finished for : Element ", ELM(K)
	WRITE(*,*) "  "
	WRITE(*,*) " ========================== "
	WRITE(*,*) " TOTAL DAMAGE = ", TDAM
	WRITE(*,*) " CRACK INITIAL CYCLES = ", CIC
	WRITE(*,*) " ========================== "
	WRITE(*,*) " "
	WRITE(*,*) "The Result & Result_data file has detail information" 

C************************ FINAL REPORT FILE *****************


	WRITE(7,*) ELM(K), TDAM, CIC, MatID(K) 
	
	



500	continue
	CLOSE(7)




	END


C
C			MAIN PROGRAM END

