from django import forms

EXP_CHOICES=[
('BDeu','BDeu'),
('K2', 'K2'),
]

class ConfigForm(forms.Form):
	n = forms.IntegerField(label='N', help_text='(No. of Nodes)', widget=forms.TextInput(attrs={'class': 'form-control'}))
	l = forms.IntegerField(label='L', help_text='(LSH Parameter)', widget=forms.TextInput(attrs={'class': 'form-control'}))
	k = forms.IntegerField(label='K', help_text='(LSH Parameter)', widget=forms.TextInput(attrs={'class': 'form-control'}))
	r = forms.IntegerField(label='r', help_text='(Accuracy Tuning Parameter)', widget=forms.TextInput(attrs={'class': 'form-control'}))
	delayConst = forms.IntegerField(label='Delay Constant', widget=forms.TextInput(attrs={'class': 'form-control'}))
	scoringFunc = forms.CharField(widget=forms.Select(choices=EXP_CHOICES, attrs={'class': 'form-control'}), label = "Scoring Function :")
	ess = forms.IntegerField(label='ESS', help_text='(Equivalent Sample Size)', widget=forms.TextInput(attrs={'class': 'form-control'}))
	familiesFile = forms.FileField(label='List of Families')
