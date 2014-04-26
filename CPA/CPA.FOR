
c***************************************************************
c	Version 3.1
C	
C	SINCE  2014.04.10
c	UPDATE 2014.04.13
c
c	CRACK PROPAGATION ANALYSIS PROGRAM 
c
c     WRITED BY  Youngchul Park  
c	                           ypark@asdl.gatech.edu
c***************************************************************

	PROGRAM CPA
	INTEGER P
	PARAMETER (P=1000000)
	DOUBLE PRECISION M(P), C(P)
	DOUBLE PRECISION FX_SM(P),FY_SM(P),FZ_SM(P) 
	DOUBLE PRECISION MX_SM(P),MY_SM(P),MZ_SM(P)
	DOUBLE PRECISION FX_SCRM(P), FY_SCRM(P), FZ_SCRM(P)
	DOUBLE PRECISION MX_SCRM(P), MY_SCRM(P), MZ_SCRM(P)
	DOUBLE PRECISION FX(P), FY(P), FZ(P),MX(P), MY(P), MZ(P)
	DOUBLE PRECISION EqStress(P), NODE_SCRM(P), L(P), SSP(P)
	INTEGER ELM_NODE(P), NODE_ELM(P), DeadCell,NextDeadCellMax


	INTEGER MatID_LEFM(P), ELM_SCRM(P),  ELM_CF , ELM_SM(P), CoNode(P)
	INTEGER ELMCRM(P), MatID(P)
	DOUBLE PRECISION stressCRM(P), stressINDEX, EqStressForce(P)
	REAL  MATno, NOSM, MM
	REAL  FXV,FYV, FZV, MXV,MYV,MZV
	INTEGER  Deadcell_ini, NextDeadCell, EqStressMax, EqStressMaxReal
	REAL	ER, Ai,	Af, Fg
	REAL A0, A1, A2, A3, XX, SIGMA, SIGMAi

C	dimension norm_stress(p,5),stress_spectrum(p,5)
      


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
CC	
C
c	### SUBROUTINE INDEX ###
C
CC
C
C***************************************************************

	WRITE(*,*)'****************************************************'
	WRITE(*,*)''
	WRITE(*,*)'       C  P  A  - Crack Propagation Analysis          '
	WRITE(*,*)''
	WRITE(*,*)'                    Version 3.1                      '
	WRITE(*,*)' '
	WRITE(*,*)'****************************************************'	


	SF=8
	NOB=10
	CF=0.001 
	MATno=4
	NOP=797330

	
C	ELM_CF =ELEMENT CRACK FOUND
      OPEN(UNIT=2,FILE='CrackFoundElement.inp')
	READ(2,*)  ELM_CF
	CLOSE(2)

	WRITE(*,*) " Crack Propagation Start for ", ELM_CF
	



	WRITE(*,*) " Read Material Data ......."

      OPEN(UNIT=1,FILE='Mat_LEFM.dat')

	DO 10, I=1,MATno
	READ(1,*)  MatID_LEFM(I), M(I)
	C(I)=1.1E-12
	WRITE(*,*) MatID_LEFM(I), M(I) ,C(I) 
10	continue	
	CLOSE(1)

	
C	To save run time *****************
C	WRITE(*,1500) " M = ",  M
C	WRITE(*,1500) " C = ",  C
C	WRITE(*,*) " "
c	WRITE(*,*) " Reading analysis factors ..... " 
c	WRITE(*,1500) " Scatter factor = ",  SF
c	WRITE(*,1500) " Number of Blocks = ",  NOB
c	WRITE(*,1500) " Conversion factor(Mpa-Ksi) = ",  CF	 	
c	WRITE(*,*) " "	

C1500	FORMAT(2X, G30.0 , 2X, G10.4 )
C	To save run time *****************
c ************************* Internal force ******************************
	WRITE(*,*) " Read Internal Forces ......."

	OPEN(unit=4, file='Internal_all_forces_SCRM.dat')
	
	DO 11, I=1,41304

	READ(4,*) ELM_SCRM(I), NODE_SCRM(I), FX_SCRM(I), FY_SCRM(I),
     +  FZ_SCRM(I) , MX_SCRM(I), MY_SCRM(I), MZ_SCRM(I)	
	
11	CONTINUE
	CLOSE(4)


c ********************** Distingush the elements type ************

		IF (ELM_CF.GT.60) THEN
		MM=4
		ELSE
		MM=3
		ENDIF 

	WRITE(*,*) " NODE NUMBER ",mm

	
	DO 14, I=1,41304
	IF (ELM_SCRM(I).EQ.ELM_CF) THEN
	GOTO 15
C	ELSE
C	WRITE(*,*) " Not available Element"
C	stop		
	ENDIF

14	CONTINUE

15	DO 17, K=1,MM	
	
			FX(K)=FX_SCRM(I)
			FY(K)=FY_SCRM(I)
			FZ(K)=FZ_SCRM(I)
			MX(K)=MX_SCRM(I)
			MY(K)=MY_SCRM(I)
			MZ(K)=MZ_SCRM(I)
	I=I+1
C	WRITE(*,*) " Forces", 	FX

17	CONTINUE

	FXV= MAXVAL(ABS(FX))
	FYV= MAXVAL(ABS(FY))
	FZV= MAXVAL(ABS(FZ))
	MXV= MAXVAL(ABS(MX))
	MYV= MAXVAL(ABS(MY))
	MZV= MAXVAL(ABS(MZ))

	WRITE(*,*) " Forces", 	FXV,FYV,FZV,MXV, MYV, MZV


	WRITE(*,*) " Read Load Spectrum ......."

C	******************** Load Spectrum ********************

	NOP=797330


	open(UNIT=5,FILE='loadspectrum.dat',STATUS='OLD')

	DO 30, I=1,NOP
	read(5,*) L(I)

30	CONTINUE
	close(5)


C ************** Element and Node realtionship **********


	open(UNIT=6,FILE='ELM_NODE.dat',STATUS='OLD')

	DO 31, I=1,21600

C	read(5,*) ELMNODE(I,1) , ELMNODE(I,2)
C	write(*,*) ELMNODE(I,1) , ELMNODE(I,2)

	read(6,*) ELM_NODE(I) , NODE_ELM(I)
31	CONTINUE
	close(6)


C ************** Stress from SCRM ******************
C	TO GET REAL STRESS
  
	OPEN(UNIT=9,FILE='stressCRM.dat')
	DO 32, I=1,7710
	
	read(9,*) ELMCRM(I),stressCRM(I), MatID(I)

	
	IF (ELMCRM(I) .EQ. ELM_CF) THEN
	GOTO 34
	ELSE 
	GOTO 33
	ENDIF
32	continue
33    stressINDEX=0.0
34	stressINDEX=stressCRM(I)
	Mat=MatID(I)



	IF (stressINDEX .EQ. 0.0) THEN
	WRITE(*,*) "********************"
	WRITE(*,*) "No Crack Propagation Region"
	WRITE(*,*) "********************"
	STOP
	ELSE
	ENDIF


C	*********************************************

C	OPEN(UNIT=8,FILE='RESULT_CPA22.OUT')

	WRITE(*,*) " Read Stress Matrix ......."


	OPEN(unit=3,file='StressMatrix.dat',STATUS='OLD')
	
C	Number of Stress Matirx
	NOSM=7200

	DO 20,J=1,NOSM

c	SM Stress Marirx
	read(3,*)ELM_SM(J),FX_SM(J),FY_SM(J),FZ_SM(J)
     +	,MX_SM(J),MY_SM(J),MZ_SM(J)
	
	   
C      WRITE(*,*) ELM_SM(J),FX_SM(J),FY_SM(J),FZ_SM(J),MX_SM(J)
C    +,MY_SM(J),MZ_SM(J)

C	**************************************************
c	Equivalent stress calculation

	EqStressForce(J)=FX_SM(J)*FXV + FY_SM(J)*FYV + FZ_SM(J)*FZV + 
     +          MX_SM(J)*MXV + MY_SM(J)*MYV + MZ_SM(J)*MZV 	

C	WRITE(8,*) EqStressForce(j)	


20	CONTINUE	

	close(3)
C	close(8)



c	Normailzed Stress calculation and adopting real stress

	EqStressMax=Maxval(EqStressForce)
	
	EqStress=(EqStressForce/EqStressMax)*stressINDEX


	WRITE(*,*) "MAX STRESS: ",  EqStressMax

	If(EqStressMax .LE. 1) THEN
	WRITE(*,*) "********************"
	WRITE(*,*) "No Crack Propagation"
	WRITE(*,*) "********************"
	STOP
	ELSE
	ENDIF

	EqStressMaxReal=Maxval(EqStress)
	WRITE(*,*) "Real MAX STRESS: ", EqStressMaxReal	

C*****************	Find initial dead cell ************
	Do 50, I=1,NOSM
	
	IF(Eqstress(I) .GE. EqStressMaxReal) THEN
	
	DeadCell_ini=ELM_SM(I)

	
	ENDIF	
	
50	CONTINUE


	OPEN(UNIT=7,FILE='RESULT_CPA_ELM.out')

C********************STRESS SPECTRUM**********
	SSP=EqStressMax*L

c ******************** CALCULATION LEFM ************************

	
c	ER = Effect Ratio to neoghboring cell
c	Ai= initila crack size
c	Af= fincal crack size for each step
c	Keff
c	C0
c	Fg= shape function
	
	ER=1.1
	Ai=0.125
	Af=Ai*2
	Fg=0.9
	ap=1
	
C	Half of yield strength
	SIGMA0=34000

C	TEST************
	N=1000
C	******************


C	******************** Neighboring Cells ************
	DeadCell=DeadCell_ini

	SIGMAi=1

	DO 500, J=1,N
	WRITE(7,*)  DeadCell
	WRITE(*,*)  J
	CALL UPCELLS(NOSM,DeadCell,EqStress,ELM_NODE,NODE_ELM,ER)
c	WRITE(*,*) "Current Dead CELL :" , DeadCell


c	OPEN(UNIT=9,FILE='RESULT_XXX.dat')
c	DO 160, I=1,NOSM
c	WRITE(9,*)  EqStress(I)
c1c60	CONTINUE
c	CLOSE(9)



	
C	******* Direction ********
	NextDeadCellMax=MaxVAL(EqStress)
c	WRITE(*,*)"kkkkk", NextDeadCellMax	


	Do 60, I=1,NOSM
	
	IF(Eqstress(I) .GE. NextDeadCellMax) THEN
	
	NextDeadCell=ELM_SM(I)
	
	else

	ENDIF	
	
60	CONTINUE

c	Write(*,*) "NextDeadCell :", NextDeadCell



	DeadCell=NextDeadCell
		
500	continue


	CLOSE(7)
C	******* Cycles to propagate 1 cell ********
	OPEN(UNIT=8,FILE='RESULT_CPA_CYC.out',STATUS='NEW')
	CLOSE(UNIT=8,STATUS='DELETE')
	OPEN(UNIT=8,FILE='RESULT_CPA_CYC.out',STATUS='NEW')	


c	******** CRACK CLOSURE MODEL ***********
	DO 600, K=1, NOP

	WRITE(*,*) K
	SIGMA=SSP(K)
	C0=C(mat)/(0.7)**m(mat)
	XX=(3.14*SIGMA)/(2*SIGMA0)
c
	R=SIGMAi/SIGMA
	A0=(0.825-0.34*ap+0.05*ap**2)*(COS(ABS(XX)))**1/ap
	A1=(0.415-0.071*ap)*SIGMA/SIGMA0
	A3=2*A0+A1-1
	A2=1-A0-A1-A3

	SRatio=A0+A1*R+A2*R**2+A3*R**3


	Keff=((1-SRatio)/(1-R))*(SIGMA-SIGMAi)*Fg*sqrt(3.14*Ai)
c	WRITE(*,*) Keff

	dela=C0*(Keff)**m(mat)
	
	cracka=Ai+dela
	
	If (cracka .GT. Af) then
	OPEN(UNIT=8,FILE='RESULT_CPA_CYC.out',STATUS='old',POSITION='APPEND')
	WRITE(8,*) K
	WRITE(*,*) " *********", K
	cracka=0
	CLOSE(8)
	ENDIF
	
	
	
C *****************************************
	


	SIGMAi=SIGMA
600	CONTINUE




	END





C			MAIN PROGRAM END




c ******************* Updating Cells **********************
C	To update neighboring cells effects
 
	SUBROUTINE UPCELLS(NOSM,DeadCell,EqStress,ELM_NODE,NODE_ELM,ER)

	INTEGER P
	PARAMETER (P=1000000)
	DOUBLE PRECISION EqStress(P), EqStress_E(P)
	REAL  NOSM
	INTEGER ELM_NODE(P), NODE_ELM(P), DeadCell, CoNode(P)
	

	OPEN(UNIT=8,FILE='CoNode.dat')


	J=1

	Do 100, I=1,21600
	

	IF(ELM_NODE(I).EQ.DeadCell) THEN
	
	CoNode(J)=NODE_ELM(I)


	WRITE(8,*)  CoNode(J)

	ELSE
	ENDIF
	
		
	J=J+1
100	continue
	close(8)

	OPEN(UNIT=8,FILE='CoNode.dat')
	DO 101, I=1,3
	READ(8,*)  CoNode(I)
c	WRITE(*,*) CoNode(I)
101	CONTINUE

	close(8)


	DO 140, J=1,3
		DO 110, I=1,21600
		
		IF (NODE_ELM(I).EQ.CoNode(J)) THEN
c		WRITE(*,*) ELM_NODE(I)

		EqStress(ELM_NODE(I))=EqStress(ELM_NODE(I))*ER
		ELSE
C		EqStressE(ELM_NODE(I))=EqStress(ELM_NODE(I))
		ENDIF
		
110		CONTINUE
140	CONTINUE

C********** Exclude current deadcell	

	EqStress(DeadCell)=0.0



c	OPEN(UNIT=9,FILE='RESULT_XXX.dat')
c	DO 160, I=1,NOSM
c	WRITE(9,*)  EqStress(I)
c160	CONTINUE
c	CLOSE(9)

	RETURN
	END